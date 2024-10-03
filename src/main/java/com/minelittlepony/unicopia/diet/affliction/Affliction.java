package com.minelittlepony.unicopia.diet.affliction;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface Affliction {
    void afflict(PlayerEntity player, ItemStack stack);

    default boolean isEmpty() {
        return getType() == AfflictionType.EMPTY;
    }

    default void appendTooltip(List<Text> tooltip) {
        tooltip.add(Text.literal(" ").append(getName()).formatted(Formatting.DARK_GRAY));
    }

    default Text getName() {
        return Text.translatable(getType().getTranslationKey());
    }

    AfflictionType<?> getType();
}
