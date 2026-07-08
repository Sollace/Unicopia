package com.minelittlepony.unicopia.entity.collision;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.entity.collision.EntityCollisions.ComplexCollidable;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public interface MultiBoundingBoxEntity extends ComplexCollidable {
    List<Box> getBoundingBoxes(Box mainBox);

    default List<Box> getGravityZoneBoxes() {
        return getBoundingBoxes(((Entity)this).getBoundingBox());
    }

    Map<Box, List<Entity>> getCollidingEntities(Stream<Box> boundingBoxes);

    SoundEvent getWalkedOnSound(double y);

    @Override
    default void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output) {
        for (Box box : getBoundingBoxes(((Entity)this).getBoundingBox())) {
            output.accept(VoxelShapes.cuboid(box));
        }
    }

    static List<Box> getBoundingBoxes(Entity entity) {
        return entity instanceof MultiBoundingBoxEntity multi ? multi.getBoundingBoxes(entity.getBoundingBox()) : List.of(entity.getBoundingBox());
    }
}
