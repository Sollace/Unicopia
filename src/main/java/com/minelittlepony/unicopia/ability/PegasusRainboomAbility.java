package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

/**
 * Pegasus ability to perform rainbooms
 */
public class PegasusRainboomAbility implements Ability<Hit> {

    @Override
    public int getWarmupTime(Pony player) {
        return 59;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 60;
    }

    @Override
    public boolean canUse(Race race) {
        return race.canInfluenceWeather();
    }

    @Nullable
    @Override
    public Optional<Hit> prepare(Pony player) {
        return Hit.of(player.canUseSuperMove() && player.getPhysics().isFlying() && !player.getMotion().isRainbooming());
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
            player.subtractEnergyCost(4);
            player.getMagicalReserves().getCharge().add(2);
            return true;
        }

        return false;
    }

    @Override
    public boolean apply(Pony player, Hit data) {

        if (prepare(player).isEmpty()) {
            return false;
        }

        if (player.consumeSuperMove()) {
            SpellType.RAINBOOM.withTraits().apply(player, CastingMethod.INNATE);
        }
        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {
        player.getMagicalReserves().getExertion().addPercent(6);
    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.spawnParticles(MagicParticleEffect.UNICORN, 5);
    }
}
