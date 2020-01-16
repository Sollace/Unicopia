package com.minelittlepony.unicopia.edibles;

import javax.annotation.Nonnull;
import com.minelittlepony.unicopia.forgebullshit.IMultiItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MultiItemEdible extends ItemEdible implements IMultiItem {

    private String translationKey;

    private final IEdible toxicityDeterminant;

    public MultiItemEdible(@Nonnull IEdible mapper) {
        super(1, 0, false);

        toxicityDeterminant = mapper;
    }

    public MultiItemEdible(String domain, String name, int amount, int saturation, @Nonnull IEdible mapper) {
        super(domain, name, amount, saturation, false);

        toxicityDeterminant = mapper;
    }

    public Item setTranslationKey(String key) {
        translationKey = key;

        return super.setTranslationKey(key);
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

    @Override
    public String[] getVariants() {
        return Toxicity.getVariants(translationKey);
    }

    @Override
    public boolean variantsAreHidden() {
        return true;
    }
}
