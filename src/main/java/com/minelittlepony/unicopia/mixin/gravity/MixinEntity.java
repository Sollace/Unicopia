package com.minelittlepony.unicopia.mixin.gravity;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.Equine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

@Mixin(value = Entity.class, priority = 29000)
abstract class MixinEntity {

    // we invert y when moving
    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3d modifyMovement(Vec3d movement) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
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

    // invert jumping velocity so we can jump
    @Inject(method = "getJumpVelocityMultiplier", at = @At("RETURN"), cancellable = true)
    private void onGetJumpVelocityMultiplier(CallbackInfoReturnable<Float> info) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            info.setReturnValue(-info.getReturnValue());
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
    @Inject(method = "spawnSprintingParticles", at = @At("HEAD"), cancellable = true)
    protected void spawnSprintingParticles(CallbackInfo info) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            eq.get().getPhysics().spawnSprintingParticles();
            info.cancel();
        }
    }

    // invert check for walking up a step
    @ModifyVariable(
            method = "adjustMovementForCollisions(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Lnet/minecraft/world/World;Ljava/util/List;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("HEAD"),
            argsOnly = true)

    private static Vec3d modifyMovementForStepheight(Vec3d movement, @Nullable Entity entity) {
        if (entity != null && movement.getY() == entity.getStepHeight()) {
            return movement.multiply(1, -1, 1);
        }
        return movement;
    }
}
