package com.minelittlepony.unicopia.diet;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UseAction;

public interface Effect extends Predicate<ItemStack> {
    Effect EMPTY = new FoodGroupEffects(List.of(), Optional.empty(), Ailment.EMPTY);

    List<FoodGroupKey> tags();

    Optional<FoodComponent> foodComponent();

    Ailment ailment();

    default void appendTooltip(ItemStack stack, List<Text> tooltip, TooltipContext context) {
        if (!test(stack)) {
            if (stack.isFood()) {
                tooltip.add(Text.literal(" ").append(Text.translatable("food_group.unicopia.misc")).formatted(Formatting.GRAY));
            } else if (stack.getUseAction() == UseAction.DRINK) {
                tooltip.add(Text.literal(" ").append(Text.translatable("food_group.unicopia.drinks")).formatted(Formatting.GRAY));
            }
        }

        if (context.isAdvanced() && stack.isFood()) {
            if (!ailment().effects().isEmpty()) {
                tooltip.add(Text.translatable("unicopia.diet.side_effects").formatted(Formatting.DARK_PURPLE));
                ailment().effects().appendTooltip(tooltip);
            }
        }
    }

    default void toBuffer(PacketByteBuf buffer) {
        buffer.writeCollection(tags(), (b, t) -> b.writeIdentifier(t.id()));
        buffer.writeOptional(foodComponent(), FoodAttributes::write);
        ailment().toBuffer(buffer);
    }

    @Override
    default boolean test(ItemStack stack) {
        return tags().stream().anyMatch(tag -> tag.contains(stack));
    }
}
