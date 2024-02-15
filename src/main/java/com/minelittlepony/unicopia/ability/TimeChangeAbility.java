package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit.Serializer;
import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.UGameRules;

public class TimeChangeAbility implements Ability<Rot> {

    @Override
    public boolean canUse(Race race) {
        return race == Race.ALICORN;
    }

    @Override
    public boolean canUse(Race.Composite race) {
        return Ability.super.canUse(race) || race.pseudo() == Race.UNICORN;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 2;
    }

    @Override
    public int getWarmupTime(Pony player) {
        return 20;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 2;
    }

    @Override
    public Serializer<Rot> getSerializer() {
        return Rot.SERIALIZER;
    }

    @Override
    public Optional<Rot> prepare(Pony player) {

        if (!player.asWorld().getGameRules().getBoolean(UGameRules.DO_TIME_MAGIC)) {
            return Optional.empty();
        }

        if (!player.subtractEnergyCost(0)) {
            return Optional.empty();
        }

        return Optional.of(Rot.of(player));
    }

    @Override
    public boolean apply(Pony player, Rot data) {
        if (!player.asWorld().getGameRules().getBoolean(UGameRules.DO_TIME_MAGIC)) {
            return false;
        }

        if (player.getSpellSlot().contains(SpellType.TIME_CONTROL)) {
            player.getSpellSlot().removeWhere(SpellType.TIME_CONTROL, true);
        } else {
            SpellType.TIME_CONTROL.withTraits().apply(player, CastingMethod.INNATE).update(player, data.applyTo(player));
        }

        return true;
    }

    @Override
    public void warmUp(Pony player, AbilitySlot slot) {

    }

    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
    }
}
