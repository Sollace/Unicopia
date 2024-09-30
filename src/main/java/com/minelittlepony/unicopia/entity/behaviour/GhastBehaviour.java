package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

public class GhastBehaviour extends MobBehaviour<GhastEntity> {

    @Override
    public void update(Pony player, GhastEntity entity, Disguise spell) {

        if (player.sneakingChanged()) {
            boolean sneaking = player.asEntity().isSneaking();
            entity.setShooting(sneaking);
            entity.setTarget(sneaking ? findTarget(player, entity) : null);

            if (sneaking) {
                if (!entity.isSilent()) {
                    entity.getWorld().syncWorldEvent(null, WorldEvents.GHAST_WARNS, entity.getBlockPos(), 0);
                }
            } else {
                if (!entity.isSilent()) {
                    entity.getWorld().syncWorldEvent(null, WorldEvents.GHAST_SHOOTS, entity.getBlockPos(), 0);
                }

                Vec3d rot = player.asEntity().getRotationVec(1);

                FireballEntity proj = new FireballEntity(entity.getWorld(), player.asEntity(), rot,
                        (int)player.getLevel().getScaled(entity.getFireballStrength())
                );
                proj.updatePosition(
                        entity.getX() + rot.x * 4,
                        entity.getBodyY(0.5D) + 0.5,
                        proj.getZ() + rot.z * 4
                );

                entity.getWorld().spawnEntity(proj);
            }
        }
    }

    @Override
    protected GhastEntity getDummy(GhastEntity entity) {
        GhastEntity dummy = super.getDummy(entity);

        Vec3d pos = entity.getCameraPosVec(1).add(entity.getRotationVec(1));
        dummy.setPos(pos.x, pos.y, pos.z);
        return dummy;
    }
}
