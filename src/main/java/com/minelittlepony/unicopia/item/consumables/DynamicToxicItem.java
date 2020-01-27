package com.minelittlepony.unicopia.item.consumables;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;

public class DynamicToxicItem extends ToxicItem {

    private final Toxic toxic;

    public DynamicToxicItem(Item.Settings settings, int hunger, float saturation, UseAction action, @Nonnull Toxic toxic) {
        super(settings, hunger, saturation, action, Toxicity.SAFE);
        this.toxic = toxic;
    }

    @Override
    public void addSecondaryEffects(PlayerEntity player, Toxicity toxicity, ItemStack stack) {
        super.addSecondaryEffects(player, toxicity, stack);
    }

    @Override
    public Toxicity getToxicity(ItemStack stack) {
        return toxic.getToxicity(stack);
    }
}
