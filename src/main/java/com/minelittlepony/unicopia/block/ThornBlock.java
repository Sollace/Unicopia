package com.minelittlepony.unicopia.block;

import java.util.Collection;
import java.util.function.Supplier;
import com.minelittlepony.unicopia.ability.EarthPonyGrowAbility;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ThornBlock extends ConnectingBlock implements EarthPonyGrowAbility.Growable, Fertilizable {
    static final Collection<BooleanProperty> PROPERTIES = FACING_PROPERTIES.values();
    static final DirectionProperty FACING = Properties.FACING;
    static final int MAX_DISTANCE = 25;
    static final int MAX_AGE = Properties.AGE_4_MAX;
    static final IntProperty DISTANCE = IntProperty.of("distance", 0, MAX_DISTANCE);
    static final IntProperty AGE = Properties.AGE_4;

    private final Supplier<Block> bud;

    public ThornBlock(Settings settings, Supplier<Block> bud) {
        super(0.125F, settings);
        this.bud = bud;
        PROPERTIES.forEach(property -> setDefaultState(getDefaultState().with(property, false)));
        setDefaultState(getDefaultState()
                .with(FACING, Direction.DOWN)
                .with(DISTANCE, 0)
                .with(AGE, 0)
                .with(DOWN, true)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(PROPERTIES.toArray(Property[]::new));
        builder.add(FACING, DISTANCE, AGE);
    }

    @Override
    @Deprecated
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(AGE) == MAX_AGE
                && random.nextInt(1200) == 0
                && world.isPlayerInRange(pos.getX(), pos.getY(), pos.getZ(), 3)) {
            UEntities.LOOT_BUG.spawn(world, pos, SpawnReason.NATURAL);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(AGE) == MAX_AGE && world.isPlayerInRange(pos.getX(), pos.getY(), pos.getZ(), 3)) {
            Vec3d particlePos = pos.toCenterPos().add(VecHelper.supply(() -> random.nextTriangular(0, 0.5)));
            world.addImportantParticle(ParticleTypes.ASH, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.get(FACING) && !state.canPlaceAt(world, pos)) {
            world.scheduleBlockTick(pos, this, 1);
        }
        return state;
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockState neighborState = world.getBlockState(pos.offset(facing));
        return (facing == Direction.DOWN && state.get(DISTANCE) == 0 && neighborState.isIn(BlockTags.DIRT))
            || neighborState.isOf(this)
            || neighborState.isOf(bud.get());
    }

    @Override
    public boolean grow(World world, BlockState state, BlockPos pos) {
        if (state.get(DISTANCE) >= MAX_DISTANCE) {
            return false;
        }

        world.setBlockState(pos, state.with(AGE, Math.min(state.get(AGE) + 1, MAX_AGE)));
        return FACING_PROPERTIES.keySet().stream()
                .filter(direction -> isConnected(state, world.getBlockState(pos.offset(direction)), direction))
                .map(direction -> {
            BlockPos p = pos.offset(direction);
            BlockState s = world.getBlockState(p);
            if (s.isAir()) {
                // sprout a new branch for cut off nodes
                world.setBlockState(p, bud.get().getDefaultState()
                        .with(FACING, direction.getOpposite())
                        .with(DISTANCE, state.get(DISTANCE) + 1)
                );
                return true;
            }
            return ((EarthPonyGrowAbility.Growable)s.getBlock()).grow(world, s, p);
        }).reduce(false, Boolean::logicalOr);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        grow(world, state, pos);
    }

    private boolean isConnected(BlockState state, BlockState neighborState, Direction direction) {
        if (!state.get(FACING_PROPERTIES.get(direction))) {
            return false;
        }
        if (neighborState.isAir()) {
            return true;
        }
        return neighborState.getBlock() instanceof EarthPonyGrowAbility.Growable
            && (neighborState.isOf(this) || neighborState.isOf(bud.get()))
            && neighborState.get(FACING) == direction.getOpposite();
    }
}
