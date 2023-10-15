package com.minelittlepony.unicopia.block;

import java.util.Collection;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class HiveBlock extends ConnectingBlock {
    static final BooleanProperty AWAKE = BooleanProperty.of("awake");
    static final Collection<BooleanProperty> PROPERTIES = FACING_PROPERTIES.values();

    public HiveBlock(Settings settings) {
        super(0.5F, settings);
        setDefaultState(getDefaultState().with(AWAKE, false));
        PROPERTIES.forEach(property -> {
            setDefaultState(getDefaultState().with(property, true));
        });
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.toArray(Property[]::new));
        builder.add(AWAKE);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        if (random.nextInt(250) == 0) {
            world.playSoundAtBlockCenter(pos, USounds.BLOCK_CHITIN_AMBIENCE, SoundCategory.BLOCKS, 0.13F, 0.2F, true);

            for (int i = 0; i < 9; i++) {
                world.addParticle(random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                        pos.getX() + random.nextDouble(),
                        pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                        random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                );
            }
        }
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        boolean connected = !neighborState.isAir();
        state = state.with(FACING_PROPERTIES.get(direction), connected);

        if (!connected) {
            return state.with(AWAKE, true);
        }

        return state;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.get(AWAKE)) {
            return;
        }

        for (var property : FACING_PROPERTIES.entrySet()) {
            if (!state.get(property.getValue())) {
                BlockPos neighborPos = pos.offset(property.getKey());
                //world.setBlockState(neighborPos, getDefaultState());
                world.setBlockState(neighborPos, UBlocks.CHITIN.getDefaultState());

                world.playSound(null, neighborPos, USounds.Vanilla.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.BLOCKS);
                world.playSound(null, neighborPos, USounds.BLOCK_CHITIN_AMBIENCE, SoundCategory.BLOCKS, 0.13F, 0.2F);

                for (int i = 0; i < 9; i++) {
                    ParticleUtils.spawnParticle(world, random.nextInt(2) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CRIMSON_SPORE,
                            neighborPos.getX() + random.nextDouble(),
                            neighborPos.getY() + 1.1, pos.getZ() + random.nextDouble(),
                            random.nextDouble() - 0.5, 0, random.nextDouble() - 0.5
                    );
                }
            }
        }
    }

    @Deprecated
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        world.scheduleBlockTick(pos, this, 15);
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    }

    @Deprecated
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return EquineContext.of(context).getSpecies() == Race.CHANGELING ? VoxelShapes.empty() : super.getCollisionShape(state, world, pos, context);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        float delta = super.calcBlockBreakingDelta(state, player, world, pos);
        delta *= Pony.of(player).getSpecies() == Race.CHANGELING ? 2 : 1;
        return delta;
    }
}
