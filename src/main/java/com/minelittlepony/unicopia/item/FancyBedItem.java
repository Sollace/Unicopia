package com.minelittlepony.unicopia.item;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.minelittlepony.unicopia.block.FancyBedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
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

    @SuppressWarnings("deprecation")
    public static FancyBedBlock.SheetPattern getPattern(ItemStack stack) {
        @Nullable
        NbtComponent blockEntityNbt = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT);
        if (blockEntityNbt == null || !blockEntityNbt.contains("pattern")) {
            return FancyBedBlock.SheetPattern.NONE;
        }
        return FancyBedBlock.SheetPattern.byId(blockEntityNbt.getNbt().getString("pattern"));
    }
}
