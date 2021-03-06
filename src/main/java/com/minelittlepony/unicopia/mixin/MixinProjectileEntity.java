package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.Equine;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;

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
