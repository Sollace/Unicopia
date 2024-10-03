package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.util.VoxelShapeUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FancyBedBlock extends BedBlock {
    public static final MapCodec<FancyBedBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("base").forGetter(b -> b.base),
            BedBlock.createSettingsCodec()
    ).apply(instance, FancyBedBlock::new));
    private static final List<Function<Direction, VoxelShape>> SHAPES = List.of(
        VoxelShapeUtil.rotator(VoxelShapes.union(
                createCuboidShape(0, 3, 1, 16, 9, 16),
                createCuboidShape(-0.5, 0, 1, 1.5, 13, 4),
                createCuboidShape(14.5, 0, 1, 16.5, 13, 4),
                createCuboidShape(1.5, 1, 0, 14.5, 16, 3)
        )),
        VoxelShapeUtil.rotator(VoxelShapes.union(
                createCuboidShape(0, 3, 0, 16, 9, 16),
                createCuboidShape(-0.5, 0, -1, 2.5, 10, 2),
                createCuboidShape(13.5, 0, -1, 16.5, 10, 2),
                createCuboidShape(1.5, 1, -2, 14.5, 12, 1)
        ))
    );

    protected final String base;

    public FancyBedBlock(String base, Settings settings) {
        super(DyeColor.WHITE, settings);
        this.base = base;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapCodec<BedBlock> getCodec() {
        return (MapCodec)CODEC;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES.get(state.get(PART).ordinal()).apply(BedBlock.getOppositePartDirection(state));
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.hasBlockEntity() && !state.isOf(newState.getBlock()) && state.get(PART) == BedPart.HEAD) {
            world.getBlockEntity(pos, UBlockEntities.FANCY_BED).ifPresent(tile -> {
                SheetPattern pattern = tile.getPattern();
                if (pattern != SheetPattern.NONE) {
                    dropStack(world, pos, BedsheetsItem.forPattern(pattern).getDefaultStack());
                }
            });
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // MC-269785
        BedPart part = state.get(PART);
        BlockPos otherHalfPos = pos.offset(getDirectionTowardsOtherPart(part, state.get(FACING)));
        BlockState otherHalfState = world.getBlockState(otherHalfPos);
        if (/*!world.isClient &&*/ player.isCreative() && part == BedPart.FOOT && otherHalfState.isOf(this) && otherHalfState.get(PART) == BedPart.HEAD) {
            if (!world.isClient) {
                world.setBlockState(otherHalfPos, otherHalfState.getFluidState().getBlockState(), Block.NOTIFY_ALL | Block.SKIP_DROPS);
            }
            spawnBreakParticles(world, player, otherHalfPos, otherHalfState);
            if (state.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
                PiglinBrain.onGuardedBlockInteracted(player, false);
            }
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
        } else {
            spawnBreakParticles(world, player, pos, state);
            if (state.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
                PiglinBrain.onGuardedBlockInteracted(player, false);
            }
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
        }
        return state;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new Tile(pos, state);
    }

    public static void setBedPattern(World world, BlockPos pos, SheetPattern pattern) {
        world.getBlockEntity(pos, UBlockEntities.FANCY_BED).ifPresent(tile -> {
            ItemStack stack = BedsheetsItem.forPattern(tile.getPattern()).getDefaultStack();
            if (!stack.isEmpty()) {
                Block.dropStack(world, pos, stack);
            }
            tile.setPattern(pattern);
            BlockState state = tile.getCachedState();
            BlockPos other = pos.offset(getDirectionTowardsOtherPart(state.get(PART), state.get(FACING)));
            world.getBlockEntity(other, UBlockEntities.FANCY_BED).ifPresent(tile2 -> {
                tile2.setPattern(pattern);
            });
        });
    }

    private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction) {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    public static class Tile extends BedBlockEntity {
        private SheetPattern pattern = SheetPattern.NONE;

        public Tile(BlockPos pos, BlockState state) {
            super(pos, state);
        }

        @Override
        public BlockEntityType<?> getType() {
            return UBlockEntities.FANCY_BED;
        }

        @Override
        public void readNbt(NbtCompound nbt, WrapperLookup lookup) {
            pattern = SheetPattern.byId(nbt.getString("pattern"));
        }

        @Override
        protected void writeNbt(NbtCompound nbt, WrapperLookup lookup) {
            nbt.putString("pattern", pattern.asString());
        }

        @Override
        public NbtCompound toInitialChunkDataNbt(WrapperLookup lookup) {
            return createNbt(lookup);
        }

        public String getBase() {
            return ((FancyBedBlock)getCachedState().getBlock()).base;
        }

        public void setPattern(SheetPattern pattern) {
            this.pattern = pattern;
            markDirty();
            if (world instanceof ServerWorld sw) {
                sw.getChunkManager().markForUpdate(getPos());
            }
        }

        public SheetPattern getPattern() {
            return pattern;
        }
    }

    public enum SheetPattern implements StringIdentifiable {
        NONE(DyeColor.WHITE),
        WHITE(DyeColor.WHITE),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        GRAY(DyeColor.GRAY),
        BLACK(DyeColor.BLACK),
        BROWN(DyeColor.BROWN),
        RED(DyeColor.RED),
        ORANGE(DyeColor.ORANGE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        GREEN(DyeColor.GREEN),
        CYAN(DyeColor.CYAN),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        BLUE(DyeColor.BLUE),
        PURPLE(DyeColor.PURPLE),
        MAGENTA(DyeColor.MAGENTA),
        PINK(DyeColor.PINK),

        APPLE(null),
        BARS(null),
        CHECKER(null),
        KELP(null),
        RAINBOW(null),
        RAINBOW_BPW(null),
        RAINBOW_BPY(null),
        RAINBOW_PBG(null),
        RAINBOW_PWR(null);

        @SuppressWarnings("deprecation")
        public static final EnumCodec<SheetPattern> CODEC = StringIdentifiable.createCodec(SheetPattern::values);

        private final String name = name().toLowerCase(Locale.ROOT);
        @Nullable
        private final DyeColor color;

        SheetPattern(@Nullable DyeColor color) {
            this.color = color;
        }

        @Nullable
        public DyeColor getColor() {
            return color;
        }

        @Override
        public String asString() {
            return name;
        }

        @SuppressWarnings("deprecation")
        public static SheetPattern byId(String id) {
            return CODEC.byId(id, NONE);
        }
    }
}
