package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Numeric;
import com.minelittlepony.unicopia.entity.player.PlayerAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.attribute.EntityAttributeInstance;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class BatPonyHangAbility implements Ability<Numeric> {

    @Override
    public int getWarmupTime(Pony player) {
        return 1;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.BAT;
    }

    @Override
    public Numeric tryActivate(Pony player) {

        EntityAttributeInstance attr = player.getOwner().getAttributeInstance(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);

        if (attr.hasModifier(PlayerAttributes.BAT_HANGING)) {
            return new Numeric(0);
        } else if (player.canHangAt()) {
            return new Numeric(1);
        }

        return null;
    }

    @Override
    public Numeric.Serializer<Numeric> getSerializer() {
        return Numeric.SERIALIZER;
    }

    @Override
    public void apply(Pony player, Numeric data) {
        EntityAttributeInstance attr = player.getOwner().getAttributeInstance(PlayerAttributes.ENTITY_GRAVTY_MODIFIER);

        if (data.type == 0 && attr.hasModifier(PlayerAttributes.BAT_HANGING)) {
            attr.removeModifier(PlayerAttributes.BAT_HANGING);
            return;
        }

        if (data.type == 1 && player.canHangAt()) {
            attr.addPersistentModifier(PlayerAttributes.BAT_HANGING);
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}
