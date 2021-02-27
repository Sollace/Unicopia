package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class AmuletItem extends WearableItem {

    private final int maxEnergy;
    private final float drain;

    private final ImmutableMultimap<EntityAttribute, EntityAttributeModifier> modifiers;

    public AmuletItem(FabricItemSettings settings, int maxEnergy, int drainRate) {
        this(settings, maxEnergy, drainRate, ImmutableMultimap.builder());
    }

    public AmuletItem(FabricItemSettings settings, int maxEnergy, int drainRate, ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> modifiers) {
        super(settings);
        this.maxEnergy = maxEnergy;
        drain = ((float)drainRate / (float)maxEnergy) / 10;

        this.modifiers = modifiers.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        /*if (world.isClient) {
            return;
        }
        if (isChargable() && entity instanceof LivingEntity && ((LivingEntity) entity).getEquippedStack(EquipmentSlot.CHEST) == stack) {
            consumeEnergy(stack, drain);
        }*/

        if (this == UItems.PEGASUS_AMULET
                && entity.world.getTime() % 6 == 0
                && entity instanceof LivingEntity
                && ((LivingEntity) entity).getEquippedStack(EquipmentSlot.CHEST) == stack
                && isApplicable((LivingEntity)entity)) {
            ParticleUtils.spawnParticles(entity.world.getDimension().isUltrawarm() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.COMPOSTER, entity, 1);
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
    public EquipmentSlot getPreferredSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return !isChargable() || stack.hasEnchantments() || getEnergy(stack) > 0;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? modifiers : ImmutableMultimap.of();
    }

    public boolean isApplicable(ItemStack stack) {
        return stack.getItem() == this && getEnergy(stack) > 0;
    }

    public boolean isApplicable(LivingEntity entity) {
        return isApplicable(entity.getEquippedStack(EquipmentSlot.CHEST));
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
}
