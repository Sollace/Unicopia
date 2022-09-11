package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.entity.duck.RotatedView;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(WorldChunk.class)
abstract class MixinWorldChunk {

    @ModifyVariable(method = {
            "getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;",
            "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            "getBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/WorldChunk$CreationType;)Lnet/minecraft/block/entity/BlockEntity;",
            "removeBlockEntity(Lnet/minecraft/util/math/BlockPos;)V",
    }, at = @At("HEAD"))
    private BlockPos modifyBlockPos(BlockPos pos) {
        return ((RotatedView)((WorldChunk)(Object)this).getWorld()).applyRotation(pos);
    }

    @ModifyVariable(method = "getFluidState(III)Lnet/minecraft/fluid/FluidState;", at = @At("HEAD"), ordinal = 1)
    private int modifyFluidPos(int y) {
        return ((RotatedView)((WorldChunk)(Object)this).getWorld()).applyRotation(y);
    }
}
