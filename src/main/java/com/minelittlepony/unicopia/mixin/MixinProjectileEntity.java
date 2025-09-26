package com.minelittlepony.unicopia.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.mob.FlyingVehicleEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Mixin(ProjectileEntity.class)
abstract class MixinProjectileEntity extends Entity {
    private MixinProjectileEntity() { super(null, null); }

    @Inject(method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onOnEntityHit(EntityHitResult hit, CallbackInfo info) {
        Equine.of(hit.getEntity()).ifPresent(eq -> {
            if (eq.onProjectileImpact((ProjectileEntity)(Object)this)) {
                info.cancel();
            }
        });
    }
}

@Mixin(ProjectileUtil.class)
abstract class MixinProjectileUtil {
    @ModifyExpressionValue(
            method = "raycast",
            at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.getRootVehicle()Lnet/minecraft/entity/Entity;", ordinal = 0)
    )
    private static Entity replaceRootVehicle(Entity rootVehicle, Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double maxDistance) {
        return rootVehicle == entity.getRootVehicle() && rootVehicle instanceof FlyingVehicleEntity ? entity : rootVehicle;
    }
}