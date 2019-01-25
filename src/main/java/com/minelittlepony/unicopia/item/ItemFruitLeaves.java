package com.minelittlepony.unicopia.item;

import net.minecraft.block.Block;

public class ItemFruitLeaves extends UItemDecoration {

    public ItemFruitLeaves(Block block, String domain, String name) {
        super(block, domain, name);
    }

    @Override
    public int getMetadata(int damage) {
        return damage | 4;
    }
}
