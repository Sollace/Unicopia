package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.BlockDestructionManager;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.duck.RotatedView;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(World.class)
abstract class MixinWorld implements WorldAccess, BlockDestructionManager.Source, RotatedView {

    private final BlockDestructionManager destructions = new BlockDestructionManager((World)(Object)this);

    private int recurseCount = 0;
    private Stack<Integer> rotations = new Stack<>();

    @Override
    public Stack<Integer> getRotations() {
        return rotations;
    }

    @Override
    public boolean hasTransform() {
        return recurseCount <= 0;
    }

    @Override
    public BlockDestructionManager getDestructionManager() {
        return destructions;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box) {
        if (box.getAverageSideLength() >= 1.0E-7D) {
            List<VoxelShape> shapes = EntityAppearance.getColissonShapes(entity, this, box);
            if (!shapes.isEmpty()) {
                return Stream.concat(shapes.stream(), WorldAccess.super.getEntityCollisions(entity, box).stream()).toList();
            }
         }

        return WorldAccess.super.getEntityCollisions(entity, box);
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
