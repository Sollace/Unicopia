package com.minelittlepony.unicopia.block;

import java.util.Locale;

import com.minelittlepony.unicopia.item.BedsheetsItem;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FancyBedBlock extends BedBlock {

    private final String base;

    public FancyBedBlock(String base, Settings settings) {
        super(DyeColor.WHITE, settings);
        this.base = base;
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
        public void readNbt(NbtCompound nbt) {
            pattern = SheetPattern.byId(nbt.getString("pattern"));
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
            nbt.putString("pattern", pattern.asString());
        }

        @Override
        public NbtCompound toInitialChunkDataNbt() {
            return createNbt();
        }

        public String getBase() {
            return ((FancyBedBlock)getCachedState().getBlock()).base;
        }

        public void setPattern(SheetPattern pattern) {
            this.pattern = pattern;
            markDirty();
        }

        public SheetPattern getPattern() {
            return pattern;
        }
    }

    public enum SheetPattern implements StringIdentifiable {
        NONE,
        APPLE,
        BARS,
        BLUE,
        CHECKER,
        ORANGE,
        PINK,
        RAINBOW;

        @SuppressWarnings("deprecation")
        public static final Codec<SheetPattern> CODEC = StringIdentifiable.createCodec(SheetPattern::values);

        private final String name = name().toLowerCase(Locale.ROOT);

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
