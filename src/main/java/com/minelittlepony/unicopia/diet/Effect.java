package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UseAction;

public interface Effect extends Predicate<ItemStack> {
    Effect EMPTY = new FoodGroupEffects(List.of(), Optional.empty(), Ailment.EMPTY);

    List<FoodGroupKey> tags();

    Optional<FoodComponent> foodComponent();

    Ailment ailment();

    default void appendTooltip(ItemStack stack, Consumer<Text> tooltip, TooltipType context) {
        if (!test(stack)) {
            if (stack.contains(DataComponentTypes.FOOD)) {
                tooltip.accept(Text.literal(" ").append(Text.translatable("food_group.unicopia.misc")).formatted(Formatting.GRAY));
            } else if (stack.getUseAction() == UseAction.DRINK) {
                tooltip.accept(Text.literal(" ").append(Text.translatable("food_group.unicopia.drinks")).formatted(Formatting.GRAY));
            }
        }

        if (context.isAdvanced() && stack.contains(DataComponentTypes.FOOD)) {
            if (!ailment().effects().isEmpty()) {
                tooltip.accept(Text.translatable("unicopia.diet.side_effects").formatted(Formatting.DARK_PURPLE));
                ailment().effects().appendTooltip(tooltip);
            }
        }
    }

    @Override
    default boolean test(ItemStack stack) {
        return tags().stream().anyMatch(tag -> tag.contains(stack));
    }
}
