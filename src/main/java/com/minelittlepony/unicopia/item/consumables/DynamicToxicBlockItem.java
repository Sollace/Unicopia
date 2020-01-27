package com.minelittlepony.unicopia.item.consumables;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;

public class DynamicToxicBlockItem extends ToxicBlockItem {

    private final Toxin toxin;

    public DynamicToxicBlockItem(Block block, Item.Settings settings, int hunger, float saturation, UseAction action, Toxicity toxicity, @Nonnull Toxin toxin) {
        super(block, settings, hunger, saturation, action, toxicity);

        this.toxin = toxin;
    }

    @Override
    public void addSecondaryEffects(PlayerEntity player, Toxicity toxicity, ItemStack stack) {
        super.addSecondaryEffects(player, toxicity, stack);
        toxin.addSecondaryEffects(player, toxicity, stack);
    }
}
