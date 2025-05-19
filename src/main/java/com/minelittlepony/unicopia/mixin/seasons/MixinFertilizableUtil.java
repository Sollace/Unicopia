package com.minelittlepony.unicopia.mixin.seasons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Pseudo
@Mixin(
        targets = { "io.github.lucaargolo.seasons.utils.FertilizableUtil" },
        remap = false
)
public interface MixinFertilizableUtil {
    @Invoker("getMultiplier")
    static float getMultiplier(ServerWorld world, BlockPos pos, BlockState state) {
        return 0;
    }
}
