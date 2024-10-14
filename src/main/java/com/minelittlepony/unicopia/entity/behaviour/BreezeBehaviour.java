package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BreezeBehaviour extends EntityBehaviour<BreezeEntity> {
    @Override
    public void update(Living<?> source, BreezeEntity entity, Disguise spell) {
        super.update(source, entity, spell);

        Entity src = source.asEntity();

        if (src.isOnGround() || src instanceof PlayerEntity player && player.getAbilities().flying) {
            return;
        }

        Vec3d vel = src.getVelocity();

        if (vel.y < 0) {
            src.setVelocity(vel.multiply(1, 0.8, 1));
        }

        if (!source.asEntity().isOnGround()) {
            vel = vel.multiply(1.2, 1, 1.2);

            src.setVelocity(MathHelper.clamp(vel.x, -0.5, 0.5), vel.y, MathHelper.clamp(vel.z, -0.5, 0.5));
        }
    }

    @Override
    public void update(Pony player, BreezeEntity entity, Disguise spell) {

        if (!player.isClient()) {

            if (!player.asEntity().hasStatusEffect(StatusEffects.JUMP_BOOST)) {
                player.asEntity().addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 10, 2, false, false));
            }
        }

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
                entity.setPose(EntityPose.SHOOTING);
            } else if (fireballsFired <= 4) {
                fireballCooldown = 6;
            } else {
                fireballCooldown = 100;
                fireballsFired = 0;
                entity.setPose(player.asEntity().isOnGround() ? EntityPose.STANDING : EntityPose.LONG_JUMPING);
            }

            if (fireballsFired > 0) {
                entity.playSound(SoundEvents.ENTITY_BREEZE_SHOOT, 1.5F, 1.0F);

                Vec3d rot = player.asEntity().getRotationVec(1);

                for (int i = 0; i < 1; ++i) {
                    BreezeWindChargeEntity proj = new BreezeWindChargeEntity(entity, entity.getWorld());
                    proj.setVelocity(rot.add(entity.getRandom().nextGaussian() * 0.1, 0, entity.getRandom().nextGaussian() * 0.1));
                    proj.setPosition(proj.getX(), entity.getBodyY(0.5D) + 0.5D, proj.getZ());
                    entity.getWorld().spawnEntity(proj);
                }
            }
        } else if (!firing) {
            entity.setPose(player.asEntity().isOnGround() ? EntityPose.STANDING : EntityPose.LONG_JUMPING);
        }

        tag.putBoolean("isFiring", firing);
        tag.putInt("fireballCooldown", fireballCooldown);
        tag.putInt("fireballsFired", fireballsFired);
    }
}
