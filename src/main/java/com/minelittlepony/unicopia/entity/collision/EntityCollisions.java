package com.minelittlepony.unicopia.entity.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.EntityView;

public class EntityCollisions {

    public static void getCollissionShapes(@Nullable Entity entity, ShapeContext context, Consumer<VoxelShape> output) {
        if (entity == null) {
            return;
        }

        if (entity.isCollidable()) {
            output.accept(VoxelShapes.cuboid(entity.getBoundingBox()));
            if (entity instanceof ComplexCollidable collidable) {
                collidable.getCollissionShapes(context, output);
            }
        } else if (entity instanceof FallingBlockEntity) {
            BlockPos pos = entity.getBlockPos();
            output.accept(((FallingBlockEntity) entity).getBlockState()
                    .getCollisionShape(entity.getWorld(), entity.getBlockPos(), context)
                    .offset(pos.getX(), pos.getY(), pos.getZ())
            );
        }
    }

    public static List<VoxelShape> getColissonShapes(@Nullable Entity entity, EntityView world, Box box) {
        ShapeContext ctx = entity == null ? ShapeContext.absent() : ShapeContext.of(entity);
        return collectCollisionBoxes(box, collector -> {
            world.getOtherEntities(entity, box.expand(50), e -> {
                Caster.of(e).flatMap(c -> c.getSpellSlot().get(SpellPredicate.IS_DISGUISE, false)).ifPresent(p -> {
                    p.getDisguise().getCollissionShapes(ctx, collector);
                });
                if (e instanceof ComplexCollidable collidable) {
                    collidable.getCollissionShapes(ctx, collector);
                }
                return false;
            });
        });
    }

    static List<VoxelShape> collectCollisionBoxes(Box box, Consumer<Consumer<VoxelShape>> generator) {
        List<VoxelShape> shapes = new ArrayList<>();
        VoxelShape entityShape = VoxelShapes.cuboid(box.expand(1.0E-6D));
        generator.accept(shape -> {
            if (!shape.isEmpty() && VoxelShapes.matchesAnywhere(shape, entityShape, BooleanBiFunction.AND)) {
                shapes.add(shape);
            }
        });
        return shapes;
    }

    public interface ComplexCollidable {
        void getCollissionShapes(ShapeContext context, Consumer<VoxelShape> output);
    }
}
