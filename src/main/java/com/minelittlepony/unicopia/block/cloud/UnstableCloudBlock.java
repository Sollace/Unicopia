package com.minelittlepony.unicopia.block.cloud;

import java.util.Optional;

import com.minelittlepony.unicopia.particle.LightningBoltParticleEffect;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class UnstableCloudBlock extends CloudBlock {
    private static final int MAX_CHARGE = 6;
    private static final IntProperty CHARGE = IntProperty.of("charge", 0, MAX_CHARGE);

    public UnstableCloudBlock(Settings settings) {
        super(settings, false);
        setDefaultState(getDefaultState().with(CHARGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        int charge = state.get(CHARGE);
        if (charge > 0) {
            world.addParticle(new LightningBoltParticleEffect(true, 10, 1, 0.6F + (charge / (float)MAX_CHARGE) * 0.4F, Optional.empty()), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0);
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.onLandedUpon(world, state, pos, entity, fallDistance);

        for (int i = 0; i < 9; i++) {
            world.addParticle(ParticleTypes.CLOUD, pos.getX() + world.random.nextFloat(), pos.getY() + world.random.nextFloat(), pos.getZ() + world.random.nextFloat(), 0, 0, 0);
        }
        world.playSound(null, pos, SoundEvents.BLOCK_WOOL_HIT, SoundCategory.BLOCKS, 1, 1);

        if (fallDistance > 3) {
            world.breakBlock(pos, true);
            return;
        }

        if (state.get(CHARGE) < MAX_CHARGE) {
            world.setBlockState(pos, state.cycle(CHARGE));
        } else {
            world.setBlockState(pos, state.with(CHARGE, 0));
            BlockPos shockPosition = pos.add(world.random.nextInt(10) - 5, -world.random.nextInt(10), world.random.nextInt(10) - 5);

            world.addImportantParticle(
                    new LightningBoltParticleEffect(false, 10, 6, 0.3F, Optional.of(shockPosition.toCenterPos())),
                    true,
                    pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    0, 0, 0
            );
            world.getOtherEntities(null, new Box(shockPosition).expand(2)).forEach(e -> {
                e.damage(entity.getDamageSources().lightningBolt(), 1);
            });

            if (world.isAir(shockPosition) && world.getBlockState(shockPosition.down()).isBurnable()) {
                world.setBlockState(shockPosition, Blocks.FIRE.getDefaultState());
            }
        }
    }
}
