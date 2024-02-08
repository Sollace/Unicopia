package com.minelittlepony.unicopia.block.zap;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ZapBlock extends Block {
    public ZapBlock(Settings settings) {
        super(settings);
    }

    @Deprecated
    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        triggerLightning(state, world, pos, player);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return getBlockBreakingDelta(super.calcBlockBreakingDelta(state, player, world, pos), player);
    }

    public static float getBlockBreakingDelta(float delta, PlayerEntity player) {
        if (Pony.of(player).getCompositeRace().canUseEarth()) {
            delta *= 50;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }

    public static void triggerLightning(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Vec3d center = Vec3d.ofCenter(pos);
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            world.getOtherEntities(null, Box.from(center).expand(7)).forEach(other -> {
                if (other instanceof ItemEntity) {
                    return;
                }
                float dist = (float)other.getPos().distanceTo(center);
                if (dist < 4) {
                    other.onStruckByLightning(serverWorld, lightning);
                } else {
                    float damage = 3 / dist;
                    if (damage > 1) {
                        other.damage(world.getDamageSources().lightningBolt(), damage);
                    }
                }
            });
        }
        world.emitGameEvent(GameEvent.LIGHTNING_STRIKE, pos, GameEvent.Emitter.of(state));
        ParticleUtils.spawnParticle(world, LightningBoltParticleEffect.DEFAULT, Vec3d.ofCenter(pos), Vec3d.ZERO);
    }
}
