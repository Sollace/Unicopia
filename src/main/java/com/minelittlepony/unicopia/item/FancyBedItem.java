package com.minelittlepony.unicopia.item;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BedItem;
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
}
