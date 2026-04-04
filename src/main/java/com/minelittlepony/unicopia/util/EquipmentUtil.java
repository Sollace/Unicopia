package com.minelittlepony.unicopia.util;

import java.util.stream.Stream;
import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface EquipmentUtil {

    static Stream<ItemStack> getInventory(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            return InventoryUtil.stream(player.getInventory());
        }

        if (entity instanceof InventoryOwner owner) {
            return InventoryUtil.stream(owner.getInventory());
        }

        return EquipmentSlot.VALUES.stream().filter(entity::hasStackEquipped).map(entity::getEquippedStack);
    }


    static Stream<ItemStack> getArmor(LivingEntity entity) {
        Stream<ItemStack> baseArmorStacks = EquipmentSlot.VALUES.stream()
                .filter(EquipmentSlot::isArmorSlot)
                .filter(entity::hasStackEquipped)
                .map(entity::getEquippedStack);
        return TrinketsDelegate.hasTrinkets() ? Stream.concat(
                TrinketsDelegate.getInstance(entity).getEquipped(entity, TrinketsDelegate.NECKLACE).map(TrinketsDelegate.EquippedStack::stack),
                baseArmorStacks
        ) : baseArmorStacks;
    }
}
