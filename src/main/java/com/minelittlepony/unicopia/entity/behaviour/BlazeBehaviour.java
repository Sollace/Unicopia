package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinBlazeEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;

public class BlazeBehaviour extends EntityBehaviour<BlazeEntity> {
    @Override
    public void update(Caster<?> source, BlazeEntity entity, Disguise spell) {
        super.update(source, entity, spell);

        Entity src = source.getEntity();

        if (src.isOnGround() || src instanceof PlayerEntity player && player.getAbilities().flying) {
            return;
        }

        Vec3d vel = src.getVelocity();

        if (vel.y < 0) {
            src.setVelocity(vel.multiply(1, 0.8, 1));
        }
    }

    @Override
    public void update(Pony player, BlazeEntity entity, Disguise spell) {

        NbtCompound tag = spell.getDisguise().getOrCreateTag();

        boolean firing = tag.getBoolean("isFiring");
        int fireballCooldown = tag.getInt("fireballCooldown");
        int fireballsFired = tag.getInt("fireballsFired");

        if (player.sneakingChanged()) {
            boolean sneaking = player.asEntity().isSneaking();

            if (sneaking) {
                firing = true;
                fireballCooldown = 0;
                fireballsFired = 0;
            } else {
                firing = false;
            }
        }

        if (firing && fireballCooldown <= 0) {
            fireballsFired++;

            if (fireballsFired == 1) {
                fireballCooldown = 60;
                ((MixinBlazeEntity)entity).invokeSetFireActive(true);
            } else if (fireballsFired <= 4) {
                fireballCooldown = 6;
            } else {
                fireballCooldown = 100;
                fireballsFired = 0;
                ((MixinBlazeEntity)entity).invokeSetFireActive(false);
            }

            if (fireballsFired > 0) {
                if (!entity.isSilent()) {
                    entity.world.syncWorldEvent(null, WorldEvents.BLAZE_SHOOTS, entity.getBlockPos(), 0);
                }

                Vec3d rot = player.asEntity().getRotationVec(1);

                for (int i = 0; i < 1; ++i) {
                   SmallFireballEntity proj = new SmallFireballEntity(entity.world, player.asEntity(),
                           rot.getX() + entity.getRandom().nextGaussian(),
                           rot.getY(),
                           rot.getZ() + entity.getRandom().nextGaussian()
                   );
                   proj.setPosition(proj.getX(), entity.getBodyY(0.5D) + 0.5D, proj.getZ());
                   entity.world.spawnEntity(proj);
                }
            }
        } else if (!firing) {
            ((MixinBlazeEntity)entity).invokeSetFireActive(false);
        }

        tag.putBoolean("isFiring", firing);
        tag.putInt("fireballCooldown", fireballCooldown);
        tag.putInt("fireballsFired", fireballsFired);
    }
}
