package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.text.Text;

public class GlassesItem extends WearableItem {
    public GlassesItem(Item.Settings settings) {
        super(settings, ArmorMaterials.LEATHER.equipSound());
    }

    @Override
    public EquipmentSlot getSlotType(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }

    public static boolean isCoolAndHasShades(LivingEntity entity) {
        ItemStack glasses = getForEntity(entity).stack();
        Text customName = glasses.get(DataComponentTypes.CUSTOM_NAME);
        return glasses.isIn(UTags.Items.TINTED_SHADES) && customName != null && "Cool Shades".equals(customName.getString());
    }

    public static TrinketsDelegate.EquippedStack getForEntity(LivingEntity entity) {
        return TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.FACE, stack -> stack.getItem() instanceof GlassesItem)
                .findFirst()
                .orElse(TrinketsDelegate.EquippedStack.EMPTY);
    }
}
