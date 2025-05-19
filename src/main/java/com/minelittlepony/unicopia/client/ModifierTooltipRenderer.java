package com.minelittlepony.unicopia.client;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.GlowableItem;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModifierTooltipRenderer {
    public static final ModifierTooltipRenderer INSTANCE = new ModifierTooltipRenderer();

    public void getTooltip(ItemStack stack, Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, List<Text> lines) {
        @Nullable
        Pony pony = Pony.of(player);

        if (pony != null) {
            pony.getDiscoveries().appendTooltip(stack, lines);
        }

        Consumer<Text> textConsumer = lines::add;

        appendTooltip(stack, UDataComponentTypes.CHARGES, context, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.ISSUER, context, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.BUTTERFLY_VARIANT, context, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.BALLOON_DESIGN, context, textConsumer, type);
        EnchantableItem.getSpellEffect(stack).appendTooltip(context, textConsumer, type);
        if (GlowableItem.isGlowing(stack)) {
            textConsumer.accept(Text.translatable("item.unicopia.friendship_bracelet.glowing").formatted(Formatting.ITALIC, Formatting.GRAY));
        }

        if (pony != null) {
            PonyDiets.getInstance().getDiet(pony).appendTooltip(stack, pony, textConsumer, type);
        }
    }

    private <T extends TooltipAppender> void appendTooltip(ItemStack stack, ComponentType<T> componentType, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type) {
        T tooltipAppender = stack.get(componentType);
        if (tooltipAppender != null) {
            tooltipAppender.appendTooltip(context, textConsumer, type);
        }
    }
}
