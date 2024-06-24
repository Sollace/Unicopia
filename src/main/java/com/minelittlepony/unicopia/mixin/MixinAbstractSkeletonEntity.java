package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.minelittlepony.unicopia.entity.ai.TargettingUtil;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(AbstractSkeletonEntity.class)
abstract class MixinAbstractSkeletonEntity extends HostileEntity {
    MixinAbstractSkeletonEntity() { super(null, null); }

    @ModifyArg(method = "shootAt(Lnet/minecraft/entity/LivingEntity;F)V", at = @At(value = "INVOKE", target = "net/minecraft/world/World.spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private Entity modifyAccuracy(Entity entity) {
        if (entity instanceof PersistentProjectileEntity projectile && getTarget() instanceof PlayerEntity player && Pony.of(player).getPhysics().isFlying()) {
            Vec3d targetPos = TargettingUtil.getProjectedPos(player)
                    .add(0, player.getHeight() * 0.33333F, 0)
                    .subtract(projectile.getPos());
            projectile.setVelocity(targetPos.x, targetPos.y + targetPos.horizontalLength() * 0.2, targetPos.z, 1.6F, (14 - getWorld().getDifficulty().getId() * 4) * 0.25F);
        }
        return entity;
    }
}
