package com.minelittlepony.unicopia.trinkets;

import java.util.UUID;

import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.WearableItem;

import dev.emi.trinkets.api.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;

public class UnicopiaTrinket implements Trinket {

    private final Item item;

    public UnicopiaTrinket(Item item) {
        this.item = item;
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity.isSpectator() || stack.isEmpty()) {
            return;
        }

        SoundEvent soundEvent = stack.getEquipSound();
        if (soundEvent != null) {
            entity.playSound(soundEvent, 1, 1);
        }
    }

    // @Override
    public int getMaxCount(ItemStack stack, SlotReference slot) {
        // https://github.com/emilyploszaj/trinkets/issues/215
        return 1;
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (item instanceof FriendshipBraceletItem && !FriendshipBraceletItem.isSigned(stack)) {
            return false;
        }

        return slot.inventory().getStack(slot.index()).isEmpty();
    }

    @Override
    public boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return !(EnchantmentHelper.hasBindingCurse(stack) && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(entity));
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        item.inventoryTick(stack, entity.world, entity, slot.index(), false);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
        Multimap<EntityAttribute, EntityAttributeModifier> modifiers = Trinket.super.getModifiers(stack, slot, entity, uuid);
        if (item instanceof WearableItem wearable) {
            item.getAttributeModifiers(wearable.getPreferredSlot(stack));
        }
        return modifiers;
    }
}
