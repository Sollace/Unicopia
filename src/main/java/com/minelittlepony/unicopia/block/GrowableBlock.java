package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;

public class GrowableBlock extends SpreadableBlock {
    public static final MapCodec<GrowableBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtils.supplierOf(Registries.BLOCK.getCodec()).fieldOf("dead").forGetter(b -> b.dead),
            BedBlock.createSettingsCodec()
    ).apply(instance, GrowableBlock::new));

    private final Supplier<Block> dead;

    protected GrowableBlock(Supplier<Block> converted, Settings settings) {
        super(settings);
        this.dead = converted;
    }

    @Override
    protected MapCodec<? extends GrowableBlock> getCodec() {
        return CODEC;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);
        if (random.nextInt(2) == 0) {
            world.addParticle(ParticleTypes.MYCELIUM, pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
        if (random.nextInt(1500) == 0) {
            world.playSoundAtBlockCenter(pos, USounds.BLOCK_CHITIN_AMBIENCE, SoundCategory.BLOCKS, 0.13F, 0.2F, true);

            for (int i = 0; i < 9; i++) {
                world.addParticle(random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                        pos.getX() + random.nextDouble(),
                        pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                        random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                );
            }
        }

        if (random.nextInt(20) == 0) {
            for (int i = 0; i < 9; i++) {
                world.addParticle(ParticleTypes.ASH,
                        pos.getX() + random.nextDouble(),
                        pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                        random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                );
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!canSurvive(state, world, pos)) {
            world.setBlockState(pos, dead.get().getDefaultState());
            return;
        }

        if (world.getLightLevel(pos.up()) >= 9) {
            BlockState blockState = getDefaultState();
            for (int i = 0; i < 4; i++) {
                BlockPos blockPos = pos.add(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );

                if (canSpread(blockState, world, blockPos)) {
                    world.setBlockState(blockPos, blockState.with(SNOWY, world.getBlockState(blockPos.up()).isOf(Blocks.SNOW)));
                }
            }
        }
    }

    private boolean canSpread(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos).isOf(dead.get())
                && !world.getFluidState(pos.up()).isIn(FluidTags.WATER)
                && canSurvive(state, world, pos);
    }

    private boolean canSurvive(BlockState state, WorldView world, BlockPos pos) {
        BlockPos above = pos.up();
        BlockState stateAbove = world.getBlockState(above);
        if (stateAbove.isOf(Blocks.SNOW) && stateAbove.get(SnowBlock.LAYERS) == 1) {
            return true;
        }
        if (stateAbove.getFluidState().getLevel() == 8) {
            return false;
        }

        return ChunkLightProvider.getRealisticOpacity(world, state, pos, stateAbove, above, Direction.UP, stateAbove.getOpacity(world, above)) < world.getMaxLightLevel();
    }
}
