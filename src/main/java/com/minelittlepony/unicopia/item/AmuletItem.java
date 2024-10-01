package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.Optional;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class AmuletItem extends WearableItem {
    public static final Identifier AMULET_MODIFIERS_ID = Unicopia.id("amulet_modifiers");

    public AmuletItem(Item.Settings settings, int maxEnergy) {
        super(settings.component(UDataComponentTypes.CHARGES, Charges.of(maxEnergy, maxEnergy)));
    }

    public AmuletItem(Item.Settings settings) {
        super(settings);
    }

    public AmuletItem(Item.Settings settings, int maxEnergy, AttributeModifiersComponent modifiers) {
        this(settings.attributeModifiers(modifiers), maxEnergy);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> list, TooltipType type) {
        for (StringVisitable line : MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(
                Text.translatable(getTranslationKey(stack) + ".lore"), 150, Style.EMPTY)) {
            MutableText compiled = Text.literal("").formatted(Formatting.ITALIC, Formatting.GRAY);
            line.visit(s -> {
                compiled.append(s);
                return Optional.empty();
            });
            list.add(compiled);
        }
        super.appendTooltip(stack, context, list, type);
    }

    @Override
    public RegistryEntry<SoundEvent> getEquipSound() {
        return ArmorMaterials.IRON.value().equipSound();
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return TrinketsDelegate.hasTrinkets() ? EquipmentSlot.OFFHAND : EquipmentSlot.CHEST;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.hasEnchantments() || Charges.of(stack).maximum() == 0 || Charges.of(stack).energy() > 0;
    }

    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() == this && (Charges.of(stack).maximum() == 0 || Charges.of(stack).energy() > 0);
    }

    public final boolean isApplicable(LivingEntity entity) {
        return !getForEntity(entity).stack().isEmpty();
    }

    public TrinketsDelegate.EquippedStack getForEntity(LivingEntity entity) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE, this::isApplicable)
                .findFirst()
                .orElse(TrinketsDelegate.EquippedStack.EMPTY);
    }

    public static TrinketsDelegate.EquippedStack get(LivingEntity entity) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE, stack -> stack.getItem() instanceof AmuletItem)
                .findFirst()
                .orElse(TrinketsDelegate.EquippedStack.EMPTY);
    }
}
