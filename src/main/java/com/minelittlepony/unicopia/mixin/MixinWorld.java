package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;

import com.minelittlepony.unicopia.BlockDestructionManager;
import com.minelittlepony.unicopia.entity.RotatedView;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(World.class)
abstract class MixinWorld implements WorldAccess, BlockDestructionManager.Source, RotatedView {

    private final BlockDestructionManager destructions = new BlockDestructionManager((World)(Object)this);

    private int rotationY;
    private int rotationIncrements;

    @Override
    public void setRotationCenter(int y, int increments) {
        rotationY = y;
        rotationIncrements = increments;
    }

    @Override
    public int getRotationY() {
        return rotationY;
    }

    @Override
    public int getRotationIncrements() {
        return rotationIncrements;
    }


    @Override
    public BlockDestructionManager getDestructionManager() {
        return destructions;
    }

    @Override
    public Stream<VoxelShape> getEntityCollisions(@Nullable Entity entity, Box box, Predicate<Entity> predicate) {
        if (box.getAverageSideLength() >= 1.0E-7D) {
            List<VoxelShape> shapes = Disguise.getColissonShapes(entity, this, box, predicate);
            if (!shapes.isEmpty()) {
                return Stream.concat(shapes.stream(), WorldAccess.super.getEntityCollisions(entity, box, predicate));
            }
         }

        return WorldAccess.super.getEntityCollisions(entity, box, predicate);
    }
}
