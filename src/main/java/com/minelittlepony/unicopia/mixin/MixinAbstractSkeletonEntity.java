package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ai.TargettingUtil;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(AbstractSkeletonEntity.class)
abstract class MixinAbstractSkeletonEntity extends HostileEntity {
    MixinAbstractSkeletonEntity() { super(null, null); }

    @Inject(
            method = "shootAt(Lnet/minecraft/entity/LivingEntity;F)V",
            at = @At(value = "INVOKE",
                target = "net/minecraft/entity/projectile/ProjectileEntity.spawnWithVelocity(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;DDDFF)Lnet/minecraft/entity/projectile/ProjectileEntity;",
                shift = Shift.AFTER))
    private void modifyAccuracy(LivingEntity target, float pullProgress, CallbackInfo info, @Local PersistentProjectileEntity projectile) {
        if (Equine.of(target).orElse(null) instanceof Pony pony && pony.getPhysics().isFlying()) {
            Vec3d targetPos = TargettingUtil.getProjectedPos(pony.asEntity())
                    .add(0, pony.asEntity().getHeight() * 0.33333F, 0)
                    .subtract(pony.asEntity().getPos());
            projectile.setVelocity(targetPos.x, targetPos.y + targetPos.horizontalLength() * 0.2, targetPos.z, 1.6F, (14 - getWorld().getDifficulty().getId() * 4) * 0.25F);
        }
    }
}
