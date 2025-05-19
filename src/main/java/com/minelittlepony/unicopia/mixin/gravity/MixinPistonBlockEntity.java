package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlockEntity.class)
abstract class MixinPistonBlockEntity {
    @Shadow
    private static Box offsetHeadBox(BlockPos pos, Box box, PistonBlockEntity blockEntity) {
        return box;
    }

    @Shadow
    private static boolean canMoveEntity(Box box, Entity entity, BlockPos pos) {
        return false;
    }

    @Shadow
    private static void moveEntity(Direction direction, Entity entity, double distance, Direction movementDirection) {

    }

    @Inject(method = "moveEntitiesInHoneyBlock", at = @At("TAIL"))
    private static void moveEntitiesInHoneyBlock(World world, BlockPos pos, float ticks, PistonBlockEntity tile, CallbackInfo info) {
        Direction direction = tile.getMovementDirection();
        if (!direction.getAxis().isHorizontal()) {
            return;
        }
        double blockY = tile.getPushedBlock().getCollisionShape(world, pos).getMin(Direction.Axis.Y);
        Box box = offsetHeadBox(pos, new Box(0, blockY - 1.5000010000000001F, 0, 1, blockY, 1), tile);
        double distance = ticks - tile.getProgress(1);
        for (Entity entity2 : world.getOtherEntities(null, box, entity -> canMoveEntity(box, entity, pos))) {
            moveEntity(direction, entity2, distance, direction);
        }
    }
}
