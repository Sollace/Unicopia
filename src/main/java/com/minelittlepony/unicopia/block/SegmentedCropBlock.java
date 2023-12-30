package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.compat.seasons.FertilizableUtil;
import com.minelittlepony.unicopia.util.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.*;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.*;

public class SegmentedCropBlock extends CropBlock implements SegmentedBlock {
    private static final Codec<Supplier<SegmentedCropBlock>> SEGMENT_CODEC = CodecUtils.supplierOf(Registries.BLOCK.getCodec().xmap(
            b -> (SegmentedCropBlock)b,
            b -> (Block)b
    ));
    public static final MapCodec<SegmentedCropBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("max_age").forGetter(b -> b.getAgeProperty().getValues().stream().mapToInt(i -> i).max().orElse(0)),
            Codec.INT.fieldOf("progression_age").forGetter(b -> b.progressionAge),
            CodecUtils.ITEM.fieldOf("seeds").forGetter(b -> b.seeds),
            SEGMENT_CODEC.optionalFieldOf("prev", null).forGetter(b -> b.prevSegmentSupplier),
            SEGMENT_CODEC.optionalFieldOf("next", null).forGetter(b -> b.nextSegmentSupplier),
            BedBlock.createSettingsCodec()
    ).apply(instance, SegmentedCropBlock::create));

    static final float BASE_GROWTH_CHANCE = /*1 in */ 50F /* chance, half the speed of regular crops */;

    private final ItemConvertible seeds;

    @Nullable
    private final Supplier<SegmentedCropBlock> prevSegmentSupplier;
    @Nullable
    private Supplier<SegmentedCropBlock> nextSegmentSupplier;

    private final int progressionAge;

    public static SegmentedCropBlock create(final int maxAge, int progressionAge,
            ItemConvertible seeds,
            @Nullable Supplier<SegmentedCropBlock> prevSegmentSupplier,
            @Nullable Supplier<SegmentedCropBlock> nextSegmentSupplier, Block.Settings settings) {

        final IntProperty age = IntProperty.of("age", 0, maxAge);
        return new SegmentedCropBlock(progressionAge, settings, seeds, prevSegmentSupplier, nextSegmentSupplier) {
            @Override
            public IntProperty getAgeProperty() {
                return age;
            }

            @Override
            public int getMaxAge() {
                return maxAge;
            }

            @Override
            protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
                builder.add(age);
            }
        };
    }

    protected SegmentedCropBlock(int progressionAge, Block.Settings settings,
            ItemConvertible seeds,
            @Nullable Supplier<SegmentedCropBlock> prevSegmentSupplier,
            @Nullable Supplier<SegmentedCropBlock> nextSegmentSupplier) {
       super(settings);
       this.seeds = seeds;
       this.prevSegmentSupplier = prevSegmentSupplier;
       this.nextSegmentSupplier = nextSegmentSupplier;
       this.progressionAge = progressionAge;
    }

    public SegmentedCropBlock createNext(int progressionAge) {
        SegmentedCropBlock next = create(getMaxAge() - this.progressionAge, progressionAge, seeds, () -> this, null, Settings.copy(this));
        nextSegmentSupplier = () -> next;
        return next;
    }

    @Override
    public MapCodec<? extends SegmentedCropBlock> getCodec() {
        return CODEC;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockPos tip = getTip(world, pos);
        BlockPos root = getRoot(world, pos);

        int height = (tip.getY() - root.getY());

        BlockState tipState = world.getBlockState(tip);
        double tipHeight = tipState.getBlock() instanceof SegmentedCropBlock tipBlock ? SegmentedBlock.getHeight(tipBlock.getAge(tipState)) : 0;

        double offset = (root.getY() - pos.getY()) * 16;

        return Block.createCuboidShape(0, offset, 0, 16, height * 16 + tipHeight + offset, 16);
    }

    @Override
    public ItemConvertible getSeedsItem() {
        return seeds;
    }

    @Override
    protected boolean canPlantOnTop(BlockState state, BlockView view, BlockPos pos) {
        return (state.getBlock() instanceof SegmentedCropBlock o && o.canSupportBlock(this, state, view, pos)) || super.canPlantOnTop(state, view, pos);
    }

    protected boolean canSupportBlock(Block other, BlockState state, BlockView view, BlockPos pos) {
        return (nextSegmentSupplier != null && nextSegmentSupplier.get() == other);
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        super.applyGrowth(world, pos, state);
        propagateGrowth(world, pos, state);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !isNext(neighborState)) {
            return state.with(getAgeProperty(), Math.min(state.get(getAgeProperty()), getMaxAge() - 1));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockPos tip = getTip(world, pos);
        BlockPos root = getRoot(world, pos);

        if (root.getY() != pos.getY()) {
            return;
        }

        if (world.getBaseLightLevel(tip, 0) >= 9) {
            int age = getAge(state);
            if (age < getMaxAge()) {
                float moisture = CropBlock.getAvailableMoisture(world.getBlockState(root).getBlock(), world, root);
                int steps = FertilizableUtil.getGrowthSteps(world, pos, state, random);
                while (steps-- > 0) {
                    if (random.nextInt((int)(BASE_GROWTH_CHANCE / moisture) + 1) == 0) {
                        world.setBlockState(pos, withAge(age + 1), Block.NOTIFY_LISTENERS);
                        propagateGrowth(world, pos, state);
                    }
                }
            }
        }
    }

    private void propagateGrowth(World world, BlockPos pos, BlockState state) {
        int oldAge = getAge(state);
        state = world.getBlockState(pos);
        int ageChange = Math.max(1, getAge(state) - oldAge);

        onGrown(world, pos, state, ageChange);

        BlockPos root = getRoot(world, pos);
        BlockPos tip = getTip(world, pos);
        for (BlockPos p : BlockPos.iterate(root, tip)) {
            if (p.getY() == pos.getY()) {
                continue;
            }

            BlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof SegmentedCropBlock segment) {
                int segAge = Math.min(segment.getAge(s) + ageChange, segment.getMaxAge());
                world.setBlockState(p, s.with(segment.getAgeProperty(), segAge));
                segment.onGrown(world, p, s, ageChange);
            }
        }
    }

    private void onGrown(World world, BlockPos pos, BlockState state, int ageChange) {
        if (nextSegmentSupplier != null && getAge(state) >= progressionAge && world.isAir(pos.up())) {
            SegmentedCropBlock nxt = nextSegmentSupplier.get();

            world.setBlockState(pos.up(), nxt.withAge(Math.min(ageChange, nxt.getMaxAge())), Block.NOTIFY_LISTENERS);
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        if (super.isFertilizable(world, pos, state)) {
            return true;
        }

        if (nextSegmentSupplier == null) {
            return false;
        }

        pos = pos.up();
        state = world.getBlockState(pos);
        return state.isAir() || (isNext(state) && state.getBlock() instanceof Fertilizable f && f.isFertilizable(world, pos, state));
    }

    @Override
    protected int getGrowthAmount(World world) {
        return super.getGrowthAmount(world) / 2;
    }

    @Override
    public boolean isBase(BlockState state) {
        return state.getBlock() == this || (prevSegmentSupplier != null && prevSegmentSupplier.get().isBase(state));
    }

    @Override
    public boolean isNext(BlockState state) {
        return state.getBlock() == this || (nextSegmentSupplier != null && nextSegmentSupplier.get().isNext(state));
    }

}
