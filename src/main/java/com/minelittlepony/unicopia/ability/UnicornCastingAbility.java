package com.minelittlepony.unicopia.ability;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.RayTraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;

/**
 * Casts magic onto the user directly, or uses the item in the main hand slot.
 * <p>
 * 1. If the player is holding nothing, defaults to toggling their equipped spell (currently only shield).
 * 2. If the player is holding a gem, consumes it and casts whatever spell is contained within onto the user.
 * 3. If the player is holding a amulet, charges it.
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
        return Hit.of(player.getMagicalReserves().getMana().get() >= getCostEstimate(player));
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        TypedActionResult<ItemStack> amulet = getAmulet(player);

        if (amulet.getResult().isAccepted()) {
            float manaLevel = player.getMagicalReserves().getMana().get();

            return Math.min(manaLevel, ((AmuletItem)amulet.getValue().getItem()).getChargeRemainder(amulet.getValue()));
        }

        TypedActionResult<CustomisedSpellType<?>> spell = player.getCharms().getSpellInHand(Hand.MAIN_HAND);

        return !spell.getResult().isAccepted() || spell.getValue().isOn(player) ? 2 : 4;
    }

    @Override
    public void apply(Pony player, Hit data) {
        TypedActionResult<ItemStack> amulet = getAmulet(player);

        if (amulet.getResult().isAccepted()) {
            ItemStack stack = amulet.getValue();
            AmuletItem item = (AmuletItem)stack.getItem();

            if (item.canCharge(stack)) {
                float amount = -Math.min(player.getMagicalReserves().getMana().get(), item.getChargeRemainder(stack));

                if (amount < 0) {
                    AmuletItem.consumeEnergy(stack, amount);
                    player.getMagicalReserves().getMana().add(amount * player.getMagicalReserves().getMana().getMax());
                    player.getReferenceWorld().playSoundFromEntity(null, player.getMaster(), USounds.ITEM_AMULET_RECHARGE, SoundCategory.PLAYERS, 1, 1);
                }
            }
        } else {
            TypedActionResult<CustomisedSpellType<?>> newSpell = player.getCharms().getSpellInHand(Hand.MAIN_HAND);

            if (newSpell.getResult() != ActionResult.FAIL) {
                CustomisedSpellType<?> spell = newSpell.getValue();

                boolean remove = player.getSpellSlot().removeIf(spell, true);
                player.subtractEnergyCost(remove ? 2 : 4);
                if (!remove) {
                    Spell s = spell.apply(player);
                    if (s == null) {
                        player.spawnParticles(ParticleTypes.LARGE_SMOKE, 6);
                        player.playSound(USounds.SPELL_CAST_FAIL, 1, 0.5F);
                    } else {
                        player.setAnimation(Animation.ARMS_UP);
                        if (s instanceof HomingSpell) {
                            RayTraceHelper.doTrace(player.getMaster(), 600, 1, EntityPredicates.CAN_COLLIDE).getEntity().ifPresent(((HomingSpell)s)::setTarget);
                        }
                        player.playSound(USounds.SPELL_CAST_SUCCESS, 0.05F, 2.2F);
                    }
                } else {
                    player.setAnimation(Animation.WOLOLO);
                }
            }
        }
    }

    private TypedActionResult<ItemStack> getAmulet(Pony player) {

        ItemStack stack = player.getMaster().getStackInHand(Hand.MAIN_HAND);

        if (stack.getItem() instanceof AmuletItem) {
            if (((AmuletItem)stack.getItem()).isChargable()) {
                return TypedActionResult.consume(stack);
            }

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExhaustion().multiply(3.3F);

        if (getAmulet(player).getResult() == ActionResult.CONSUME) {
            Vec3d eyes = player.getMaster().getCameraPosVec(1);

            float i = player.getAbilities().getStat(slot).getFillProgress();

            Random rng = player.getReferenceWorld().random;
            player.addParticle(i > 0.5F ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes, VecHelper.supply(() -> (rng.nextGaussian() - 0.5) / 10));
            player.playSound(USounds.ITEM_AMULET_CHARGING, 1, i / 20);
        } else {
            player.spawnParticles(MagicParticleEffect.UNICORN, 5);
        }
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
