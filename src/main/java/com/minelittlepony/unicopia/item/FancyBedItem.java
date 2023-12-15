package com.minelittlepony.unicopia.item;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.block.FancyBedBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

public class FancyBedItem extends BedItem implements Supplier<BlockEntity> {

    private final Supplier<BlockEntity> renderEntity;

    public FancyBedItem(Block block, Settings settings) {
        super(block, settings);
        this.renderEntity = Suppliers.memoize(() -> ((BlockEntityProvider)block).createBlockEntity(BlockPos.ORIGIN, block.getDefaultState()));
    }

    @Override
    public BlockEntity get() {
        return renderEntity.get();
    }

    public static FancyBedBlock.SheetPattern getPattern(ItemStack stack) {
        @Nullable
        NbtCompound blockEntityNbt = getBlockEntityNbt(stack);
        if (blockEntityNbt == null || !blockEntityNbt.contains("pattern", NbtElement.STRING_TYPE)) {
            return FancyBedBlock.SheetPattern.NONE;
        }
        return FancyBedBlock.SheetPattern.byId(blockEntityNbt.getString("pattern"));
    }
}
