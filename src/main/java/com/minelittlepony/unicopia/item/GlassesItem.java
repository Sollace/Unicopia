package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;

public class GlassesItem extends WearableItem {
    public GlassesItem(FabricItemSettings settings) {
        super(settings);
    }

    @Override
    public SoundEvent getEquipSound() {
        return ArmorMaterials.LEATHER.getEquipSound();
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public boolean isApplicable(LivingEntity entity) {
        return getForEntity(entity).getItem() == this;
    }

    public static ItemStack getForEntity(LivingEntity entity) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE)
                .filter(stack -> stack.getItem() instanceof GlassesItem)
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }
}
