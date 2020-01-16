package com.minelittlepony.unicopia.block;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;

public class SugarBlock extends FallingBlock {
    public SugarBlock(String domain, String name) {
        super(FabricBlockSettings.of(Material.SAND)
                .strength(10, 10)
                .hardness(0.7F)
                .sounds(BlockSoundGroup.SAND)
                .build()
        );
        // setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        //TODO:
        // Loot table drops:
        // // Items.SUGAR x 9;
    }
}
