package com.minelittlepony.unicopia.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.core.SpeciesList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.Projectile;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;

@Mixin(ProjectileEntity.class)
public abstract class MixinProjectileEntity extends Entity implements Projectile {

    public MixinProjectileEntity() { super(null, null); }

    @Inject(method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onOnEntityHit(EntityHitResult hit, CallbackInfo info) {
        SpeciesList.instance().getForEntity(hit.getEntity())
        .ifPresent(container -> {
            if (container.getRaceContainer().onProjectileImpact((ProjectileEntity)(Object)this)) {
                info.cancel();
            }
        });
    }
}
