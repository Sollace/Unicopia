package com.minelittlepony.unicopia.ability;

import java.util.Optional;

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
    public Optional<Multi> prepare(Pony player) {

        if (player.getAcrobatics().isHanging()) {
            return Optional.of(new Multi(BlockPos.ZERO, 0));
        }

        return TraceHelper.findBlock(player.asEntity(), 5, 1)
                .map(BlockPos::down)
                .filter(player.getAcrobatics()::canHangAt)
                .map(pos -> new Multi(pos, 1));
    }

    @Override
    public Multi.Serializer<Multi> getSerializer() {
        return Multi.SERIALIZER;
    }

    @Override
    public boolean apply(Pony player, Multi data) {
        if (data.hitType() == 0 && player.getAcrobatics().isHanging()) {
            player.getAcrobatics().stopHanging();
            return true;
        }

        if (data.hitType() == 1 && player.getAcrobatics().canHangAt(data.pos().pos())) {
            player.getAcrobatics().startHanging(data.pos().pos());
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
