package com.minelittlepony.unicopia.diet.affliction;

import java.util.function.Consumer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface Affliction {
    void afflict(PlayerEntity player, ItemStack stack);

    default boolean isEmpty() {
        return getType() == AfflictionType.EMPTY;
    }

    default void appendTooltip(Consumer<Text> tooltip) {
        tooltip.accept(Text.literal(" ").append(getName()).formatted(Formatting.DARK_GRAY));
    }

    default Text getName() {
        return Text.translatable(getType().getTranslationKey());
    }

    AfflictionType<?> getType();
}
