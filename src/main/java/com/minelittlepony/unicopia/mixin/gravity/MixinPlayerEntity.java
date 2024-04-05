package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.Equine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Mixin(PlayerEntity.class)
abstract class MixinPlayerEntity {
    @ModifyVariable(method = "adjustMovementForSneaking", at = @At("HEAD"), argsOnly = true)
    private Vec3d flipMovementForSneaking(Vec3d movement) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return movement.multiply(1, -1, 1);
        }
        return movement;
    }

    @Inject(method = "adjustMovementForSneaking", at = @At("RETURN"), cancellable = true)
    private void unflipMovementForSneaking(Vec3d movement, MovementType type, CallbackInfoReturnable<Vec3d> info) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            info.setReturnValue(info.getReturnValue().multiply(1, -1, 1));
        }
    }

    @ModifyArg(method = { "adjustMovementForSneaking", "method_30263" }, at = @At(
            value = "INVOKE",
            target = "net/minecraft/util/math/Box.offset(DDD)Lnet/minecraft/util/math/Box;"),
            index = 1)
    private double invertStepHeight(double stepHeight) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return -stepHeight;
        }
        return stepHeight;
    }

    @ModifyArg(method = "canChangeIntoPose",
            at = @At(value = "INVOKE",
                target = "net/minecraft/world/World.isSpaceEmpty(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Z"))
    private Box unicopiaReAnchorBoundingBox(Box box) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            Entity self = eq.get().asEntity();
            Box oldBox = self.getBoundingBox();
            double newHeight = box.getLengthY();
            if (newHeight > oldBox.getLengthY()) {
                double targetMaxY = oldBox.maxY;
                Vec3d min = new Vec3d(box.minX, targetMaxY - newHeight, box.minZ);
                Vec3d max = new Vec3d(box.maxX, targetMaxY, box.maxZ);
                return new Box(min, max);
            }
        }
        return box;
    }
}
