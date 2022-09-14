package com.minelittlepony.unicopia.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.UEntityAttributes;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.enchantment.AttributedEnchantment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

public class ModifierTooltipRenderer implements ItemTooltipCallback {

    @Override
    public void getTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {

        int flags = stack.hasNbt() && stack.getNbt().contains("HideFlags", 99) ? stack.getNbt().getInt("HideFlags") : 0;

        if (isShowing(flags, ItemStack.TooltipSection.MODIFIERS)) {

            Map<EquipmentSlot, Multimap<EntityAttribute, EntityAttributeModifier>> modifiers = new HashMap<>();

            Equine.<PlayerEntity, Pony>of(MinecraftClient.getInstance().player).ifPresent(eq -> {
                getEnchantments(stack).filter(p -> p.getRight() instanceof AttributedEnchantment).forEach(pair -> {
                    ((AttributedEnchantment)pair.getRight()).getModifiers(eq, pair.getLeft(), modifiers);
                });
            });

            modifiers.forEach((slot, modifs) -> {

                List<Text> newLines = new ArrayList<>();

                modifs.entries().stream()
                    .filter(entry -> entry.getKey().equals(EntityAttributes.GENERIC_MOVEMENT_SPEED) || UEntityAttributes.REGISTRY.contains(entry.getKey()))
                    .forEach(entry -> describeModifiers(entry.getKey(), entry.getValue(), null, newLines));

                if (!newLines.isEmpty()) {
                    Text find = new TranslatableText("item.modifiers." + slot.getName()).formatted(Formatting.GRAY);
                    int insertPosition = getInsertPosition(stack, find, flags, lines, context.isAdvanced());
                    if (insertPosition == -1) {
                        lines.add(LiteralText.EMPTY);
                        lines.add(find);
                        lines.addAll(newLines);
                    } else {
                        lines.addAll(insertPosition, newLines);
                    }
                }
            });
        }

        if (MinecraftClient.getInstance().player != null) {
            Pony.of(MinecraftClient.getInstance().player).getDiscoveries().appendTooltip(stack, MinecraftClient.getInstance().world, lines);
        }
    }

    private int getInsertPosition(ItemStack stack, Text category, int flags, List<Text> lines, boolean advanced) {
        int insertPosition = lines.indexOf(category);

        if (insertPosition > -1) {
            return insertPosition + 1;
        }

        if (insertPosition == -1 && stack.hasNbt()) {
            if (isShowing(flags, ItemStack.TooltipSection.MODIFIERS) && stack.getNbt().getBoolean("Unbreakable")) {
                insertPosition = checkFor(lines, new TranslatableText("item.unbreakable").formatted(Formatting.BLUE));
            }

            if (insertPosition == -1 && isShowing(flags, ItemStack.TooltipSection.CAN_DESTROY) && stack.getNbt().contains("CanDestroy", 9)) {
                insertPosition = checkFor(lines, new TranslatableText("item.canBreak").formatted(Formatting.GRAY));
            }

            if (insertPosition == -1 && isShowing(flags, ItemStack.TooltipSection.CAN_PLACE) && stack.getNbt().contains("CanPlaceOn", 9)) {
                insertPosition = checkFor(lines, new TranslatableText("item.canPlace").formatted(Formatting.GRAY));
            }
        }

        if (insertPosition == -1 && advanced) {
           if (stack.isDamaged()) {
               insertPosition = checkFor(lines, new TranslatableText("item.durability", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
           } else {
               insertPosition = checkFor(lines, new LiteralText(Registry.ITEM.getId(stack.getItem()).toString()).formatted(Formatting.DARK_GRAY));
           }
        }

        return insertPosition;
    }

    private int checkFor(List<Text> lines, Text category) {
        return lines.indexOf(category);
    }

    private void describeModifiers(EntityAttribute attribute, EntityAttributeModifier modifier, @Nullable PlayerEntity player, List<Text> lines) {
        double value = modifier.getValue();
        boolean baseAdjusted = false;
        if (player != null) {
            value += player.getAttributeBaseValue(attribute);
            baseAdjusted = true;
        }

        Operation op = modifier.getOperation();

        double displayValue;
        if (op != EntityAttributeModifier.Operation.MULTIPLY_BASE && op != EntityAttributeModifier.Operation.MULTIPLY_TOTAL) {
            displayValue = value;
        } else {
            displayValue = value * 100;
        }

        if (baseAdjusted) {
            lines.add(new LiteralText(" ").append(getModifierLineBase("equals", displayValue, op, attribute, Formatting.DARK_GREEN)));
        } else if (value > 0) {
            lines.add(getModifierLineBase("plus", displayValue, op, attribute, attribute == UEntityAttributes.ENTITY_GRAVTY_MODIFIER ? Formatting.RED : Formatting.BLUE));
        } else if (value < 0) {
            lines.add(getModifierLineBase("take", -displayValue, op, attribute, attribute == UEntityAttributes.ENTITY_GRAVTY_MODIFIER ? Formatting.BLUE : Formatting.RED));
        }
    }

    private Text getModifierLineBase(String root, double displayValue, Operation op, EntityAttribute attribute, Formatting color) {
        return new TranslatableText("attribute.modifier." + root + "." + op.getId(),
                ItemStack.MODIFIER_FORMAT.format(displayValue),
                new TranslatableText(attribute.getTranslationKey())
            ).formatted(color);
    }


    private static boolean isShowing(int flags, ItemStack.TooltipSection section) {
        return (flags & section.getFlag()) == 0;
    }

    private static Stream<Pair<Integer, Enchantment>> getEnchantments(ItemStack stack) {
        if (!stack.isEmpty()) {
            return stack.getEnchantments()
                    .stream()
                    .map(t -> (NbtCompound)t)
                    .map(tag -> Registry.ENCHANTMENT.getOrEmpty(Identifier.tryParse(tag.getString("id")))
                                .map(ench -> new Pair<>(tag.getInt("lvl"), ench)))
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }

        return Stream.empty();
    }

}
