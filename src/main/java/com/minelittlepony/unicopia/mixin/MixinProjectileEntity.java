package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ducks.PonyContainer;

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
        PonyContainer.of(hit.getEntity()).ifPresent(container -> {
            if (container.get().onProjectileImpact((ProjectileEntity)(Object)this)) {
                info.cancel();
            }
        });
    }
}
