package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.client.render.PlayerPoser.Animation;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;
import com.minelittlepony.unicopia.util.RayTraceHelper;

import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

/**
 * Fires a spell as a projectile.
 *
 * 1. If the player is holding nothing, casts their equipped offensive spell (currently only vortex - inverse of shield)
 * 2. If the player is holding a gem, consumes it and casts whatever spell is contained within onto a projectile.
 */
public class UnicornProjectileAbility implements Ability<Hit> {

    @Override
    public Identifier getIcon(Pony player, boolean swap) {
        Identifier id = Abilities.REGISTRY.getId(this);
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + (swap ? "_focused" : "_unfocused") + ".png");
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 8;
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
    public Hit tryActivate(Pony player) {
        return Hit.of(player.getCharms().getSpellInHand(Hand.OFF_HAND).getResult() != ActionResult.FAIL);
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 7;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type) {
        if (type == ActivationType.DOUBLE_TAP) {
            if (!player.isClient()) {
                TypedActionResult<CustomisedSpellType<?>> thrown = player.getCharms().getSpellInHand(Hand.OFF_HAND);

                if (thrown.getResult() != ActionResult.FAIL) {
                    thrown.getValue().create().toThrowable().throwProjectile(player).ifPresent(projectile -> {
                        player.subtractEnergyCost(getCostEstimate(player));
                        player.setAnimation(Animation.ARMS_FORWARD, 2);
                    });
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public void apply(Pony player, Hit data) {
        TypedActionResult<CustomisedSpellType<?>> thrown = player.getCharms().getSpellInHand(Hand.MAIN_HAND);

        if (thrown.getResult() != ActionResult.FAIL) {


            Spell spell = thrown.getValue().create();

            spell.toThrowable().throwProjectile(player).ifPresent(projectile -> {
                player.subtractEnergyCost(getCostEstimate(player));
                player.setAnimation(Animation.ARMS_FORWARD);
                projectile.setHydrophobic();

                if (spell instanceof HomingSpell) {
                    RayTraceHelper.doTrace(player.getMaster(), 600, 1, EntityPredicates.EXCEPT_SPECTATOR).getEntity().filter(((HomingSpell)spell)::setTarget).ifPresent(target -> {
                        projectile.setHomingTarget(target);
                    });
                }
            });
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExhaustion().multiply(3.3F);
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
