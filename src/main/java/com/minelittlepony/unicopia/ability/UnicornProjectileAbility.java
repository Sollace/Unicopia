package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.TraceHelper;

import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

/**
 * Fires a spell as a projectile.
 *
 * 1. If the player is holding nothing, casts their equipped offensive spell (currently only vortex - inverse of shield)
 * 2. If the player is holding a gem, consumes it and casts whatever spell is contained within onto a projectile.
 */
public class UnicornProjectileAbility extends AbstractSpellCastingAbility {
    @Override
    public int getWarmupTime(Pony player) {
        return 8;
    }

    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(player.getCharms().getSpellInHand(false).getResult() != ActionResult.FAIL);
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 7;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type, Optional<Hit> data) {
        if (type == ActivationType.DOUBLE_TAP) {
            if (!player.isClient()) {
                TypedActionResult<CustomisedSpellType<?>> thrown = player.getCharms().getSpellInHand(true);

                if (thrown.getResult() != ActionResult.FAIL) {
                    thrown.getValue().create().toThrowable().throwProjectile(player).ifPresent(projectile -> {
                        player.subtractEnergyCost(getCostEstimate(player));
                        player.setAnimation(Animation.ARMS_FORWARD, Animation.Recipient.ANYONE, 2);
                    });
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean apply(Pony player, Hit data) {
        TypedActionResult<CustomisedSpellType<?>> thrown = player.getCharms().getSpellInHand(true);

        if (thrown.getResult() != ActionResult.FAIL) {
            Spell spell = thrown.getValue().create();

            spell.toThrowable().throwProjectile(player).ifPresent(projectile -> {
                player.subtractEnergyCost(getCostEstimate(player));
                player.setAnimation(Animation.ARMS_FORWARD, Animation.Recipient.ANYONE);
                projectile.setHydrophobic();

                if (spell instanceof HomingSpell) {
                    TraceHelper.findEntity(player.asEntity(), 600, 1).filter(((HomingSpell)spell)::setTarget).ifPresent(projectile::setHomingTarget);
                }
            });

            return true;
        }

        return false;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExhaustion().multiply(3.3F);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
