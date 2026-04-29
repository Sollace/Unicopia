package com.minelittlepony.unicopia.client;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.diet.PonyDiets;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.GlowableItem;
import com.minelittlepony.unicopia.item.component.MimicComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModifierTooltipRenderer {
    public static final ModifierTooltipRenderer INSTANCE = new ModifierTooltipRenderer();

    public void getTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, @Nullable PlayerEntity player, TooltipType type, Consumer<Text> textConsumer) {
        @Nullable
        Pony pony = Pony.of(player);

        if (pony != null && displayComponent.shouldDisplay(UDataComponentTypes.SPELL_TRAITS)) {
            pony.getDiscoveries().appendTooltip(stack, textConsumer);
        }

        appendTooltip(stack, UDataComponentTypes.CHARGES, context, displayComponent, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.ISSUER, context, displayComponent, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.BUTTERFLY_VARIANT, context, displayComponent, textConsumer, type);
        appendTooltip(stack, UDataComponentTypes.BALLOON_DESIGN, context, displayComponent, textConsumer, type);
        if (displayComponent.shouldDisplay(UDataComponentTypes.MIMIC)) {
            MimicComponent.appendTooltip(stack, context, textConsumer, type);
        }
        if (displayComponent.shouldDisplay(UDataComponentTypes.STORED_SPELL)) {
            EnchantableItem.getSpellEffect(stack).appendTooltip(context, textConsumer, type, stack);
        }
        if (displayComponent.shouldDisplay(UDataComponentTypes.GLOWING) && GlowableItem.isGlowing(stack)) {
            textConsumer.accept(Text.translatable("item.unicopia.friendship_bracelet.glowing").formatted(Formatting.ITALIC, Formatting.GRAY));
        }

        if (pony != null && displayComponent.shouldDisplay(DataComponentTypes.FOOD)) {
            PonyDiets.getInstance().getDiet(pony).appendTooltip(stack, pony, textConsumer, type);
        }
    }

    private void appendTooltip(ItemStack stack, ComponentType<? extends TooltipAppender> componentType, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        appendTooltip(stack, componentType, context, displayComponent, textConsumer, type, null);
    }

    private void appendTooltip(ItemStack stack, ComponentType<? extends TooltipAppender> componentType, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type, TooltipAppender fallback) {
        if (displayComponent.shouldDisplay(componentType)) {
            TooltipAppender tooltipAppender = stack.getOrDefault(componentType, fallback);
            if (tooltipAppender != null) {
                tooltipAppender.appendTooltip(context, textConsumer, type, stack);
            }
        }
    }
}
