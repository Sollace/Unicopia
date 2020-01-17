package com.minelittlepony.unicopia.item.consumables;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class MultiItemEdible extends ItemEdible {

    private final IEdible toxicityDeterminant;

    public MultiItemEdible(Item.Settings settings, @Nonnull IEdible mapper) {
        super(settings);

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
