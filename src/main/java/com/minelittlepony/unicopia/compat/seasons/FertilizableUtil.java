package com.minelittlepony.unicopia.compat.seasons;

import com.minelittlepony.unicopia.mixin.seasons.MixinFertilizableUtil;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface FertilizableUtil {
    boolean MOD_LOADED = FabricLoader.getInstance().isModLoaded("seasons");

    static float getMultiplier(ServerWorld world, BlockPos pos, BlockState state) {
        if (MOD_LOADED) {
            return MixinFertilizableUtil.getMultiplier(world, pos, state);
        }
        return 0;
    }

    static int getGrowthSteps(ServerWorld world, BlockPos pos, BlockState state, Random random) {
        float multiplier = 1 + FertilizableUtil.getMultiplier(world, pos, state);
        int steps = 0;

        while (multiplier > 0) {
            if (multiplier >= random.nextFloat()) {
                multiplier -= 1;
                steps++;
            }
        }

        return steps;
    }
}
