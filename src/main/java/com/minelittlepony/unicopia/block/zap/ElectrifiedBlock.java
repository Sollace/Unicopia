package com.minelittlepony.unicopia.block.zap;

import java.util.Optional;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface ElectrifiedBlock {

    default void spawnElectricalParticles(World world, BlockPos pos, Random random) {
        world.addParticle(new LightningBoltParticleEffect(true, 10, 1, 0.6F, Optional.empty()),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                0, 0, 0
        );
    }

    default float getBlockBreakingDelta(float delta, PlayerEntity player) {
        if (Pony.of(player).getCompositeRace().canUseEarth()) {
            delta *= 50;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }

    default void triggerLightning(BlockState state, World world, BlockPos pos) {
        Vec3d center = pos.toCenterPos();
        if (world instanceof ServerWorld serverWorld) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.EVENT);
            world.getOtherEntities(null, Box.from(center).expand(7)).forEach(entity -> {
                shockEntity(serverWorld, center, lightning, entity);
            });
        }
        world.emitGameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Emitter.of(state));
        ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, center, Vec3d.ZERO);
    }

    default void triggerLightning(BlockState state, World world, BlockPos pos, LivingEntity entity, boolean knockBack) {
        Vec3d center = pos.toCenterPos();
        if (world instanceof ServerWorld serverWorld) {
            shockEntity(serverWorld, center, EntityType.LIGHTNING_BOLT.create(world, SpawnReason.EVENT), entity);
        }
        if (knockBack) {
            Vec3d offset = center.subtract(entity.getPos());
            entity.takeKnockback(0.8, offset.x, offset.z);
        }
        world.emitGameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Emitter.of(state));
        ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, center, Vec3d.ZERO);
    }

    private static void shockEntity(ServerWorld serverWorld, Vec3d center, LightningEntity lightning, Entity entity) {
        if (entity instanceof ItemEntity) {
            return;
        }
        float dist = (float)entity.getPos().distanceTo(center);
        if (dist < 4) {
            entity.onStruckByLightning(serverWorld, EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.EVENT));
        } else {
            float damage = 3 / dist;
            if (damage > 1) {
                entity.damage(serverWorld, entity.getDamageSources().lightningBolt(), damage);
            }
        }
    }
}
