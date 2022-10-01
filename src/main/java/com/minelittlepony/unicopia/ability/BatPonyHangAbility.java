package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Multi;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.TraceHelper;

import net.minecraft.util.math.BlockPos;

/**
 * A magic casting ability for unicorns.
 * (only shields for now)
 */
public class BatPonyHangAbility implements Ability<Multi> {

    @Override
    public int getWarmupTime(Pony player) {
        return 1;
    }

    @Override
    public int getCooldownTime(Pony player) {
        return 0;
    }

    @Override
    public double getCostEstimate(Pony player) {
        return 0;
    }

    @Override
    public boolean canUse(Race race) {
        return race == Race.BAT;
    }

    @Override
    public Multi tryActivate(Pony player) {

        if (player.isHanging()) {
            return new Multi(BlockPos.ZERO, 0);
        }

        return TraceHelper.findBlock(player.getMaster(), 5, 1)
                .map(BlockPos::down)
                .filter(player::canHangAt)
                .map(pos -> new Multi(pos, 1))
                .orElse(null);
    }

    @Override
    public Multi.Serializer<Multi> getSerializer() {
        return Multi.SERIALIZER;
    }

    @Override
    public void apply(Pony player, Multi data) {
        if (data.hitType == 0 && player.isHanging()) {
            player.stopHanging();
            return;
        }

        if (data.hitType == 1 && player.canHangAt(data.pos())) {
            player.startHanging(data.pos());
        }
    }

    @Override
    public void preApply(Pony player, AbilitySlot slot) {
    }

    @Override
    public void postApply(Pony player, AbilitySlot slot) {
    }
}
