package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;

/**
 * Dashing ability for flying creatures.
 */
public class FlyingDashAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 19;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 30;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.HIPPOGRIFF;
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(player.getPhysics().isFlying());
    }

    @Override
    public Hit.Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean onQuickAction(Pony player, ActivationType type, Optional<Hit> data) {

        if (type == ActivationType.TAP && !player.getMotion().isRainbooming() && player.getPhysics().isFlying() && player.getMagicalReserves().getMana().get() > 40) {
            player.getPhysics().dashForward((float)player.asWorld().random.nextTriangular(2.5F, 0.3F));
            return true;
        }

        return false;
    }

    @Override
    public boolean apply(Pony player, Hit data) {
        player.getPhysics().dashForward((float)player.asWorld().random.nextTriangular(2.5F, 0.3F));
        player.subtractEnergyCost(2);
        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        float velocityScale = player.getAbilities().getStat(slot).getCooldown();
        float multiplier = 1 + (0.02F * velocityScale);
        player.asEntity().getVelocity().multiply(multiplier, 1, multiplier);
    }
}
