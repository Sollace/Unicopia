package com.minelittlepony.unicopia.toxin;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.UseAction;

public class ToxicBlockItem extends BlockItem {

    public ToxicBlockItem(Block block, Settings settings, Toxic toxic) {
        super(block, settings);
        ((ToxicHolder)this).setToxic(toxic);
    }

    public ToxicBlockItem(Block block, Settings settings, UseAction action, Toxicity toxicity, Toxin toxin) {
        super(block, settings);
        ((ToxicHolder)this).setToxic(new Toxic(this, action, toxin, toxicity));
    }
}
