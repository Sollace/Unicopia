package com.minelittlepony.unicopia.mixin.gravity;

import java.util.Stack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.duck.RotatedView;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(World.class)
abstract class MixinWorld implements WorldAccess, RotatedView {

    private int recurseCount = 0;
    private final Stack<Integer> rotations = new Stack<>();

    @Override
    public Stack<Integer> getRotations() {
        return rotations;
    }

    @Override
    public boolean hasTransform() {
        return recurseCount <= 0;
    }

    @ModifyVariable(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"))
    private BlockPos modifyBlockPos(BlockPos pos) {
        pos = applyRotation(pos);
        recurseCount = Math.max(0, recurseCount) + 1;
        return pos;
    }

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("RETURN"))
    public void onSetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> info) {
        recurseCount = Math.max(0, recurseCount - 1);
    }
}
