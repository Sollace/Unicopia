package com.minelittlepony.unicopia.mixin;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(World.class)
abstract class MixinWorld implements WorldAccess {
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
