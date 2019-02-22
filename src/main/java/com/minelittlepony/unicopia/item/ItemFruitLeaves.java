package com.minelittlepony.unicopia.item;

import net.minecraft.block.Block;

public class ItemFruitLeaves extends UItemDecoration {

    public ItemFruitLeaves(Block block) {
        super(block);
    }

    @Override
    public int getMetadata(int damage) {
        return damage | 4;
    }
}
