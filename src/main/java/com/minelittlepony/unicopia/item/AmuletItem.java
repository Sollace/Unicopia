package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.trinkets.TrinketsDelegate;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class AmuletItem extends WearableItem implements ChargeableItem {

    private final int maxEnergy;

    private final ImmutableMultimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public AmuletItem(FabricItemSettings settings, int maxEnergy) {
        this(settings, maxEnergy, ImmutableMultimap.of());
    }

    public AmuletItem(FabricItemSettings settings, int maxEnergy, ImmutableMultimap<EntityAttribute, EntityAttributeModifier> modifiers) {
        super(settings);
        this.maxEnergy = maxEnergy;
        this.modifiers = modifiers;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext tooltipContext) {

        for (StringVisitable line : MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(
                Text.translatable(getTranslationKey(stack) + ".lore"), 150, Style.EMPTY)) {
            MutableText compiled = Text.literal("").formatted(Formatting.ITALIC, Formatting.GRAY);
            line.visit(s -> {
                compiled.append(s);
                return Optional.empty();
            });
            list.add(compiled);
        }

        if (isChargable()) {
            list.add(Text.translatable("item.unicopia.amulet.energy", (int)Math.floor(ChargeableItem.getEnergy(stack)), getMaxCharge()));
        }
    }

    @Override
    public SoundEvent getEquipSound() {
        return ArmorMaterials.IRON.getEquipSound();
    }

    @Override
    public EquipmentSlot getPreferredSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return !isChargable() || stack.hasEnchantments() || ChargeableItem.getEnergy(stack) > 0;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? modifiers : ImmutableMultimap.of();
    }

    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() == this && (!isChargable() || ChargeableItem.getEnergy(stack) > 0);
    }

    public boolean isApplicable(LivingEntity entity) {
        return isApplicable(getForEntity(entity));
    }

    @Override
    public int getMaxCharge() {
        return maxEnergy;
    }

    public static ItemStack getForEntity(LivingEntity entity) {
        return TrinketsDelegate.getInstance().getEquipped(entity, TrinketsDelegate.NECKLACE)
                .filter(stack -> stack.getItem() instanceof AmuletItem)
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }

    public static class ModifiersBuilder {
        private static final UUID SLOT_UUID = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");

        private final ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> modifiers = new ImmutableMultimap.Builder<>();

        public ModifiersBuilder add(EntityAttribute attribute, double amount) {
            modifiers.put(attribute, new EntityAttributeModifier(SLOT_UUID, "Armor modifier", amount, EntityAttributeModifier.Operation.ADDITION));
            return this;
        }

        public ImmutableMultimap<EntityAttribute, EntityAttributeModifier> build() {
            return modifiers.build();
        }
    }
}
