package com.minelittlepony.unicopia.ability;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.AmuletItem;
import com.minelittlepony.unicopia.item.ChargeableItem;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.TraceHelper;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

/**
 * Casts magic onto the user directly, or uses the item in the main hand slot.
 * <p>
 * 1. If the player is holding nothing, defaults to toggling their equipped spell (currently only shield).
 * 2. If the player is holding a gem, consumes it and casts whatever spell is contained within onto the user.
 * 3. If the player is holding a amulet, charges it.
 */
public class UnicornCastingAbility extends AbstractSpellCastingAbility {

    @Override
    public int getWarmupTime(Pony player) {
        return 20;
    }

    @Override
    @Nullable
    public Hit tryActivate(Pony player) {
        if (!player.canCast()) {
            return null;
        }
        return Hit.of(player.getMagicalReserves().getMana().get() >= getCostEstimate(player));
    }

    @Override
    public double getCostEstimate(Pony player) {
        TypedActionResult<ItemStack> amulet = getAmulet(player);

        if (amulet.getResult().isAccepted()) {
            float manaLevel = player.getMagicalReserves().getMana().get();

            return Math.min(manaLevel, ((AmuletItem)amulet.getValue().getItem()).getChargeRemainder(amulet.getValue()));
        }

        TypedActionResult<CustomisedSpellType<?>> spell = player.getCharms().getSpellInHand(false);

        return !spell.getResult().isAccepted() || spell.getValue().isOn(player) ? 2 : 4;
    }

    @Override
    public int getColor(Pony player) {
        TypedActionResult<ItemStack> amulet = getAmulet(player);
        if (amulet.getResult().isAccepted()) {
            return 0x000000;
        }

        return super.getColor(player);
    }

    @Override
    public void apply(Pony player, Hit data) {
        if (!player.canCast()) {
            return;
        }

        TypedActionResult<ItemStack> amulet = getAmulet(player);

        if (amulet.getResult().isAccepted()) {
            ItemStack stack = amulet.getValue();
            ChargeableItem item = (ChargeableItem)stack.getItem();

            if (item.canCharge(stack)) {
                float amount = -Math.min(player.getMagicalReserves().getMana().get(), item.getChargeRemainder(stack));

                if (amount < 0) {
                    ChargeableItem.consumeEnergy(stack, amount);
                    player.getMagicalReserves().getMana().add(amount * player.getMagicalReserves().getMana().getMax());
                    player.asWorld().playSoundFromEntity(null, player.asEntity(), USounds.ITEM_AMULET_RECHARGE, SoundCategory.PLAYERS, 1, 1);
                }
            }
        } else {
            TypedActionResult<CustomisedSpellType<?>> newSpell = player.getCharms().getSpellInHand(true);

            if (newSpell.getResult() != ActionResult.FAIL) {
                CustomisedSpellType<?> spell = newSpell.getValue();

                boolean removed = player.getSpellSlot().removeWhere(s -> {
                    return s.findMatches(spell).findAny().isPresent() && (spell.isEmpty() || !SpellType.PLACED_SPELL.test(s));
                }, true);
                player.subtractEnergyCost(removed ? 2 : 4);
                if (!removed) {
                    Spell s = spell.apply(player);
                    if (s == null) {
                        player.spawnParticles(ParticleTypes.LARGE_SMOKE, 6);
                        player.playSound(USounds.SPELL_CAST_FAIL, 1, 0.5F);
                    } else {
                        player.setAnimation(Animation.ARMS_UP, Animation.Recipient.HUMAN);
                        if (s instanceof HomingSpell homer) {
                            TraceHelper.findEntity(player.asEntity(), homer.getRange(player), 1, EntityPredicates.VALID_ENTITY).ifPresent(homer::setTarget);
                        }
                        player.playSound(USounds.SPELL_CAST_SUCCESS, 0.05F, 2.2F);
                    }
                } else {
                    player.setAnimation(Animation.WOLOLO, Animation.Recipient.ANYONE);
                }
            }
        }
    }

    private TypedActionResult<ItemStack> getAmulet(Pony player) {

        ItemStack stack = player.asEntity().getStackInHand(Hand.MAIN_HAND);

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
            Vec3d eyes = player.asEntity().getCameraPosVec(1);

            float i = player.getAbilities().getStat(slot).getFillProgress();

            Random rng = player.asWorld().random;
            player.addParticle(i > 0.5F ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD, eyes, VecHelper.supply(() -> (rng.nextGaussian() - 0.5) / 10));
            player.playSound(USounds.ITEM_AMULET_CHARGING, 1, i / 20);
        } else {
            player.spawnParticles(MagicParticleEffect.UNICORN, 5);
        }
    }
}
