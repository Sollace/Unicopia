package com.minelittlepony.unicopia.item.cloud;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;

public class CloudBedItem extends CloudBlockItem {

    private final Supplier<BlockEntity> renderEntity;

    public CloudBedItem(Block block, Settings settings) {
        super(block, settings);
        this.renderEntity = Suppliers.memoize(() -> ((BlockEntityProvider)block).createBlockEntity(BlockPos.ORIGIN, block.getDefaultState()));
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        return context.getWorld().setBlockState(context.getBlockPos(), state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD | Block.FORCE_STATE);
    }

    public BlockEntity getRenderEntity() {
        return renderEntity.get();
    }
}
