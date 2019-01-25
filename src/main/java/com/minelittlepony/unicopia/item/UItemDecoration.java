package com.minelittlepony.unicopia.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class UItemDecoration extends ItemBlock {

    public UItemDecoration(Block block, String domain, String name) {
        super(block);

        setTranslationKey(name);
        setRegistryName(domain, name);
        setCreativeTab(block.getCreativeTab());
    }
}
