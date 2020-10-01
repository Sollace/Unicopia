package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.Vec3d;

public class GhastBehaviour extends MobBehaviour<GhastEntity> {

    @Override
    public void update(Pony player, GhastEntity entity, DisguiseSpell spell) {

        if (player.sneakingChanged()) {
            boolean sneaking = player.getOwner().isSneaking();
            entity.setShooting(sneaking);
            entity.setTarget(sneaking ? findTarget(player, entity) : null);

            if (sneaking) {
                if (!entity.isSilent()) {
                    entity.world.syncWorldEvent(null, 1015, entity.getBlockPos(), 0);
                }
            } else {
                Vec3d vec3d = entity.getRotationVec(1);

                if (!entity.isSilent()) {
                    entity.world.syncWorldEvent(null, 1016, entity.getBlockPos(), 0);
                }

                Vec3d rot = player.getEntity().getRotationVec(1);

                FireballEntity fireballEntity = new FireballEntity(entity.world, entity, rot.getX(), rot.getY(), rot.getZ());
                fireballEntity.explosionPower = entity.getFireballStrength();
                fireballEntity.setOwner(player.getOwner());
                fireballEntity.updatePosition(entity.getX() + vec3d.x * 4, entity.getBodyY(0.5D) + 0.5D, fireballEntity.getZ() + vec3d.z * 4);

                entity.world.spawnEntity(fireballEntity);
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
