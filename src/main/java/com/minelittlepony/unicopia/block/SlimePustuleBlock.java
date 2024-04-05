package com.minelittlepony.unicopia.block;

import java.util.Arrays;
import java.util.Locale;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.USounds;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class SlimePustuleBlock extends Block {
    public static final MapCodec<SlimePustuleBlock> CODEC = createCodec(SlimePustuleBlock::new);
    public static final EnumProperty<Shape> SHAPE = EnumProperty.of("shape", Shape.class);
    private static final BooleanProperty POWERED = Properties.POWERED;
    private static final Direction[] DIRECTIONS = Arrays.stream(Direction.values())
            .filter(direction -> direction != Direction.UP)
            .toArray(Direction[]::new);
    private static final VoxelShape SHAFT_SHAPE = Block.createCuboidShape(7.5, 0, 7.5, 8.5, 16, 8.5);
    private static final VoxelShape DRIP_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(7, 10, 7, 9, 16, 9),
            Block.createCuboidShape(3, 15, 4, 9, 16, 10),
            Block.createCuboidShape(7, 15, 7, 12, 16, 12)
    );
    private static final VoxelShape BULB_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(4, 1, 4, 12, 10, 12),
            Block.createCuboidShape(5, 10, 5, 11, 13, 11),
            Block.createCuboidShape(6, 13, 6, 10, 15, 10),
            Block.createCuboidShape(7, 13, 7, 9, 20, 9)
    );
    private static final VoxelShape CAP_SHAPE = VoxelShapes.union(SHAFT_SHAPE, DRIP_SHAPE);
    private static final Vector3f DUST_COLOR = new Vector3f(1, 0.2F, 0.1F);

    public SlimePustuleBlock(Settings settings) {
        super(settings.ticksRandomly());
        setDefaultState(getDefaultState().with(SHAPE, Shape.DRIP).with(POWERED, false));
    }

    @Override
    protected MapCodec<? extends SlimePustuleBlock> getCodec() {
        return CODEC;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(SHAPE)) {
            case POD -> BULB_SHAPE;
            case DRIP -> DRIP_SHAPE;
            case CAP -> CAP_SHAPE;
            case STRING -> SHAFT_SHAPE;
        };
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        super.randomDisplayTick(state, world, pos, random);

        if (state.get(POWERED)) {

            VoxelShape shape = state.getCullingShape(world, pos);
            float x = (float)MathHelper.lerp(random.nextFloat(), shape.getMin(Axis.X), shape.getMax(Axis.X));
            float z = (float)MathHelper.lerp(random.nextFloat(), shape.getMin(Axis.Z), shape.getMax(Axis.Z));
            world.addParticle(new DustParticleEffect(DUST_COLOR, 1),
                    pos.getX() + x,
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + z, 0, 0, 0);
        }

        if (random.nextInt(15) == 0) {
            VoxelShape shape = state.getCullingShape(world, pos);
            float x = (float)MathHelper.lerp(random.nextFloat(), shape.getMin(Axis.X), shape.getMax(Axis.X));
            float z = (float)MathHelper.lerp(random.nextFloat(), shape.getMin(Axis.Z), shape.getMax(Axis.Z));
            world.addParticle(ParticleTypes.DRIPPING_HONEY,
                    pos.getX() + x,
                    pos.getY() + random.nextDouble(),
                    pos.getZ() + z, 0, 0, 0);
        }
    }

    @Deprecated
    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(SHAPE) == Shape.POD && random.nextInt(130) == 0) {
            SlimeEntity slime = EntityType.SLIME.create(world);
            slime.setSize(1, true);
            slime.setPosition(pos.toCenterPos());
            world.spawnEntity(slime);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (state.get(SHAPE) == Shape.POD) {
            world.getOtherEntities(player.canHarvest(state) ? player : null, new Box(pos).expand(1)).forEach(entity -> {
                entity.damage(entity.getDamageSources().inFire(), 2);
                entity.setFireTicks(3);
            });

            world.playSound(null, pos, USounds.BLOCK_SLIME_PUSTULE_POP, SoundCategory.BLOCKS, 5, 1);
            for (int i = 0; i < 8; i++) {
                world.addParticle(ParticleTypes.LAVA,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        world.random.nextGaussian() * 1.5F,
                        0,
                        world.random.nextGaussian() * 1.5F
                );
                world.addParticle(ParticleTypes.CRIT,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        world.random.nextGaussian() * 1.5F,
                        world.random.nextGaussian() * 1.5F,
                        world.random.nextGaussian() * 1.5F
                );
                world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        world.random.nextGaussian() * 1.5F,
                        world.random.nextGaussian() * 1.5F,
                        world.random.nextGaussian() * 1.5F
                );
            }

            return state;
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, POWERED);
    }

    @Deprecated
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        pos = pos.up();
        state = world.getBlockState(pos);
        return state.isOf(this) || state.isSideSolid(world, pos, Direction.DOWN, SideShapeType.CENTER);
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!canPlaceAt(state, world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        if (direction.getAxis() == Direction.Axis.Y) {
            Shape currentShape = state.get(SHAPE);

            if (direction == Direction.DOWN && (currentShape == Shape.CAP || currentShape == Shape.STRING)) {
                return state.with(POWERED, getReceivedRedstonePower(world, pos) > 0);
            }

            Shape shape = determineShape(world, pos);
            return state.with(SHAPE, shape).with(POWERED, getReceivedRedstonePower(world, pos) > 0);
        }

        return state.with(POWERED, getReceivedRedstonePower(world, pos) > 0);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Shape shape = determineShape(ctx.getWorld(), ctx.getBlockPos());
        return super.getPlacementState(ctx)
                .with(SHAPE, shape == Shape.STRING ? Shape.POD : shape)
                .with(POWERED, getReceivedRedstonePower(ctx.getWorld(), ctx.getBlockPos()) > 0);
    }

    @Override
    @Deprecated
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (state.isOf(this) && newState.isOf(this) && state.get(POWERED) != newState.get(POWERED)) {
            world.updateNeighborsAlways(pos.up(), this);
        }
    }

    @Override
    @Deprecated
    public boolean emitsRedstonePower(BlockState state) {
        return state.get(POWERED);
    }

    @Override
    @Deprecated
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (direction == Direction.DOWN && emitsRedstonePower(state)) {
            return 15;
        }
        return 0;
    }

    @Override
    @Deprecated
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (direction == Direction.DOWN && emitsRedstonePower(state)) {
            return 15;
        }
        return 0;
    }

    private int getReceivedRedstonePower(BlockView world, BlockPos pos) {
        int power = 0;
        for (Direction direction : DIRECTIONS) {
            power = Math.max(power, world.getBlockState(pos.offset(direction)).getStrongRedstonePower(world, pos, direction));
            if (power >= 15) {
                return Math.min(15, power);
            }
        }
        return Math.min(15, power);
    }

    private Shape determineShape(WorldAccess world, BlockPos pos) {
        BlockState above = world.getBlockState(pos.up());
        BlockState below = world.getBlockState(pos.down());

        boolean hasAbove = above.isOf(this);
        boolean hasRoof = !hasAbove && above.isSideSolid(world, pos.up(), Direction.DOWN, SideShapeType.CENTER);
        boolean hasBelow = below.isOf(this);

        if (hasRoof && below.isAir()) {
            return Shape.DRIP;
        }
        if (hasRoof && hasBelow) {
            return Shape.CAP;
        }

        if ((hasRoof || hasAbove) && hasBelow) {
            return Shape.STRING;
        }

        return Shape.POD;
    }

    public enum Shape implements StringIdentifiable {
        DRIP,
        CAP,
        STRING,
        POD;

        private final String name = name().toLowerCase(Locale.ROOT);

        @Override
        public String asString() {
            return name;
        }
    }
}
