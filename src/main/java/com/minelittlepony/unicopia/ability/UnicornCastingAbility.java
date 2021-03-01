package com.minelittlepony.unicopia.ability;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Streams;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.Attached;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.GemstoneItem;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class UnicornCastingAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 20;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canCast();
    }

    @Override
    @Nullable
    public Hit tryActivate(Pony player) {
        return getCostEstimate(player) <= player.getMagicalReserves().getMana().get() ? Hit.INSTANCE : null;
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return getAmulet(player)
                .map(pair -> Math.min(player.getMagicalReserves().getMana().get(), pair.getLeft().getChargeRemainder(pair.getRight())))
                .orElseGet(() -> player.hasSpell() && getNewSpell(player).isPresent() ? 4F : 2F);
    }

    @Override
    public void apply(Pony player, Hit data) {
        getAmulet(player).filter(pair -> {
            float amount = -Math.min(player.getMagicalReserves().getMana().get(), pair.getLeft().getChargeRemainder(pair.getRight()));

            if (amount < 0) {
                AmuletItem.consumeEnergy(pair.getRight(), amount);
                player.getMagicalReserves().getMana().add(amount * player.getMagicalReserves().getMana().getMax());
                player.getWorld().playSoundFromEntity(null, player.getMaster(), SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.PLAYERS, 1, 1);
            }
            return true;
        }).orElseGet(() -> {
            Optional<Spell> newSpell = getNewSpell(player);

            @Nullable
            Spell spell = player.hasSpell() ? newSpell.orElse(null) : newSpell.orElseGet(SpellType.SHIELD::create);

            player.subtractEnergyCost(spell == null ? 2 : 4);
            player.setSpell(spell);

            return null;
        });
    }

    private Optional<Pair<AmuletItem, ItemStack>> getAmulet(Pony player) {

        ItemStack stack = player.getMaster().getStackInHand(Hand.MAIN_HAND);

        if (stack.getItem() instanceof AmuletItem) {
            AmuletItem amulet = (AmuletItem)stack.getItem();
            if (amulet.canCharge(stack)) {
                return Optional.of(new Pair<>(amulet, stack));

            }
        }

        return Optional.empty();
    }

    private Optional<Spell> getNewSpell(Pony player) {
        final SpellType<?> current = player.hasSpell() ? player.getSpell(true).getType() : null;
        return Streams.stream(player.getMaster().getItemsHand())
                .flatMap(stack -> GemstoneItem.consumeSpell(stack, player.getMaster(), current, i -> i instanceof Attached))
                .findFirst();
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getEnergy().multiply(3.3F);

        if (getAmulet(player).isPresent()) {
            Vec3d eyes = player.getMaster().getCameraPosVec(1);

            float i = player.getAbilities().getStat(slot).getFillProgress();

            Random rng = player.getWorld().random;

            player.getWorld().addParticle(i > 0.5F ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes.x, eyes.y, eyes.z,
                    (rng.nextGaussian() - 0.5) / 10,
                    (rng.nextGaussian() - 0.5) / 10,
                    (rng.nextGaussian() - 0.5) / 10
            );
            player.getWorld().playSound(player.getEntity().getX(), player.getEntity().getY(), player.getEntity().getZ(), SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.PLAYERS, 1, i / 20, true);
        } else {
            player.spawnParticles(MagicParticleEffect.UNICORN, 5);
        }
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
