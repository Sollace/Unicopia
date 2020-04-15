package com.minelittlepony.unicopia.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemUsageContext;

public final class HoeUtil extends HoeItem {
    private HoeUtil() {
        super(null, 0, null);
    }

    public static void registerTillingAction(Block block, BlockState tilledState) {
        TILLED_BLOCKS.put(block, tilledState);
    }

    @FunctionalInterface
    public interface Tillable {
        boolean canTill(ItemUsageContext context);
    }
}
