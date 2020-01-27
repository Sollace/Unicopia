package com.minelittlepony.unicopia.item.consumables;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;

public class DelegatedEdibleItem extends EdibleItem {

    private final IEdible toxicityDeterminant;

    public DelegatedEdibleItem(Item.Settings settings, UseAction action, @Nonnull IEdible mapper) {
        super(settings, action);

        toxicityDeterminant = mapper;
    }

    @Override
    public void addSecondaryEffects(PlayerEntity player, Toxicity toxicity, ItemStack stack) {
        super.addSecondaryEffects(player, toxicity, stack);

        toxicityDeterminant.addSecondaryEffects(player, toxicity, stack);
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return toxicityDeterminant.getToxicityLevel(stack);
    }
}
