package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;

public class GlassesItem extends WearableItem {
    public GlassesItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public RegistryEntry<SoundEvent> getEquipSound() {
        return ArmorMaterials.LEATHER.value().equipSound();
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public boolean isApplicable(LivingEntity entity) {
        return getForEntity(entity).stack().isOf(this);
    }

    public static TrinketsDelegate.EquippedStack getForEntity(LivingEntity entity) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE, stack -> stack.getItem() instanceof GlassesItem)
                .findFirst()
                .orElse(TrinketsDelegate.EquippedStack.EMPTY);
    }
}
