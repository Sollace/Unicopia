package com.minelittlepony.unicopia.mixin.gravity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.Equine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Mixin(value = Entity.class, priority = 29000)
abstract class MixinEntity {

    // we invert y when moving
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3d modifyMovement(Vec3d movement) {
        if (unicopiaIsGravityInverted()) {
            return movement.multiply(1, -1, 1);
        }
        return movement;
    }

    // fix on ground check
    @Inject(method = "move", at = @At(value = "FIELD", target = "net/minecraft/entity/Entity.groundCollision:Z", shift = Shift.AFTER, ordinal = 0))
    private void onUpdateOnGroundFlag(MovementType movementType, Vec3d movement, CallbackInfo info) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            eq.get().asEntity().groundCollision = eq.get().asEntity().verticalCollision && movement.y > 0.0;
        }
    }

    // invert offsets so it can properly find the block we're walking on
    @ModifyVariable(method = "getPosWithYOffset", at = @At("HEAD"), argsOnly = true)
    private float onGetPosWithYOffset(float offset) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return -(eq.get().asEntity().getHeight() + offset);
        }
        return offset;
    }

    // fix sprinting particles
    @ModifyArg(method = "spawnSprintingParticles",
            at = @At(value = "INVOKE",
                target = "net/minecraft/world/World.addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"),
            index = 2)
    private double modifyParticleY(double y) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            Entity self = eq.get().asEntity();
            return self.getHeight() - y + (self.getY() * 2);
        }
        return y;
    }

    // fix fall damage
    @ModifyArg(
            method = "move",
            at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.fall(DZLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V"))
    private double modifyFallDistance(double heightDifference) {
        if (unicopiaIsGravityInverted()) {
            return -heightDifference;
        }
        return heightDifference;
    }

    // invert check for walking up a step
    @ModifyVariable(
            method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("HEAD"),
            argsOnly = true)

    private static Vec3d modifyMovementForStepheight(Vec3d movement, @Nullable Entity entity) {
        if (entity instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative() && movement.getY() == entity.getStepHeight()) {
            return movement.multiply(1, -1, 1);
        }
        return movement;
    }

    @Inject(method = {"calculateBoundsForPose"}, at = @At("RETURN"), cancellable = true)
    private void adjustPoseBoxForGravity(EntityPose pos, CallbackInfoReturnable<Box> info) {
        unicopiaReAnchorBoundingBox(info);
    }

    @Inject(method = {"calculateBoundingBox"}, at = @At("RETURN"), cancellable = true)
    private void adjustPoseBoxForGravity(CallbackInfoReturnable<Box> info) {
        if (unicopiaReAnchorBoundingBox(info)) {
            Entity self = (Entity)(Object)this;
            self.setPos(self.getX(), info.getReturnValue().minY, self.getZ());
        }
    }

    private boolean unicopiaReAnchorBoundingBox(CallbackInfoReturnable<Box> info) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            Entity self = eq.get().asEntity();
            Box box = info.getReturnValue();
            Box oldBox = self.getBoundingBox();
            double newHeight = box.getYLength();
            if (newHeight > oldBox.getYLength()) {
                double targetMaxY = oldBox.maxY;
                Vec3d min = new Vec3d(box.minX, targetMaxY - newHeight, box.minZ);
                Vec3d max = new Vec3d(box.maxX, targetMaxY, box.maxZ);
                info.setReturnValue(new Box(min, max));
                return true;
            }
        }
        return false;
    }

    private boolean unicopiaIsGravityInverted() {
        return this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative();
    }
}
