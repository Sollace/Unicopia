package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class AmuletItem extends ArmorItem {

    private final int maxEnergy;
    private final float drain;

    private final ImmutableMultimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public AmuletItem(Item.Settings settings, int maxEnergy, int drainRate) {
        this(settings, maxEnergy, drainRate, ImmutableMultimap.builder());
    }

    public AmuletItem(Item.Settings settings, int maxEnergy, int drainRate, ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> modifiers) {
        super((Settings)settings, EquipmentSlot.CHEST, settings);
        this.maxEnergy = maxEnergy;
        drain = ((float)drainRate / (float)maxEnergy) / 10;

        this.modifiers = modifiers.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (isChargable() && entity instanceof LivingEntity && ((LivingEntity) entity).getEquippedStack(getSlotType()) == stack) {
            consumeEnergy(stack, drain);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> list, TooltipContext tooltipContext) {

        for (StringVisitable line : MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(
                new TranslatableText(getTranslationKey(stack) + ".lore"), 150, Style.EMPTY)) {
            MutableText compiled = new LiteralText("").formatted(Formatting.ITALIC, Formatting.GRAY);
            line.visit(s -> {
                compiled.append(s);
                return Optional.empty();
            });
            list.add(compiled);
        }

        if (isChargable()) {
            list.add(new TranslatableText("item.unicopia.amulet.energy", (int)Math.floor(getEnergy(stack)), maxEnergy));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return !isChargable() || stack.hasEnchantments() || getEnergy(stack) > 0;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == getSlotType() ? modifiers : ImmutableMultimap.of();
    }

    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() == this && getEnergy(stack) > 0;
    }

    public boolean isApplicable(LivingEntity entity) {
        return isApplicable(entity.getEquippedStack(getSlotType()));
    }

    public boolean isChargable() {
        return maxEnergy > 0;
    }

    public boolean canCharge(ItemStack stack) {
        return isChargable() && getEnergy(stack) < maxEnergy;
    }

    public float getChargeRemainder(ItemStack stack) {
        return Math.max(0, maxEnergy - getEnergy(stack));
    }

    public static void consumeEnergy(ItemStack stack, float amount) {
        setEnergy(stack, getEnergy(stack) - amount);
    }

    public static float getEnergy(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("energy") ? stack.getTag().getFloat("energy") : 0;
    }

    public static void setEnergy(ItemStack stack, float energy) {
        if (energy <= 0) {
            stack.removeSubTag("energy");
        } else {
            stack.getOrCreateTag().putFloat("energy", energy);
        }
    }

    public static class Settings extends FabricItemSettings implements ArmorMaterial {

        private final String name;
        private int protection;
        private float toughness;
        private float resistance;

        public Settings(String name) {
            this.name = name;
        }

        public Settings protection(int protection) {
            this.protection = protection;
            return this;
        }

        public Settings toughness(int toughness) {
            this.toughness = toughness;
            return this;
        }

        public Settings resistance(int resistance) {
            this.resistance = resistance;
            return this;
        }

        @Override
        public int getDurability(EquipmentSlot slot) {
            return ArmorMaterials.LEATHER.getDurability(slot);
        }

        @Override
        public int getProtectionAmount(EquipmentSlot slot) {
            return protection;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public float getToughness() {
            return toughness;
        }

        @Override
        public float getKnockbackResistance() {
            return resistance;
        }
    }
}
