package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.ability.data.Hit.Serializer;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;

public class TimeChangeAbility implements Ability<Hit> {

    @Override
    public boolean canUse(Race race) {
        return race == Race.ALICORN;
    }

    @Override
    public boolean canUse(Race.Composite race) {
        return race.pseudo() == Race.UNICORN;
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
    public Serializer<Hit> getSerializer() {
        return Hit.SERIALIZER;
    }

    @Override
    public Optional<Hit> prepare(Pony player) {
        if (!player.subtractEnergyCost(0)) {
            return Optional.empty();
        }

        return Hit.INSTANCE;
    }

    @Override
    public boolean apply(Pony player, Hit data) {

        if (player.getSpellSlot().contains(SpellType.TIME_CONTROL)) {
            player.getSpellSlot().removeWhere(SpellType.TIME_CONTROL, true);
        } else {
            SpellType.TIME_CONTROL.withTraits().apply(player, CastingMethod.INNATE);
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
