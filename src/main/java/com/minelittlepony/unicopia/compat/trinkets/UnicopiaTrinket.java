package com.minelittlepony.unicopia.compat.trinkets;

import com.google.common.collect.Multimap;
import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.WearableItem;

import dev.emi.trinkets.api.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;

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

        if (!(stack.getItem() instanceof ItemTracker.Trackable)) {
            Equipment q = Equipment.fromStack(stack);
            RegistryEntry<SoundEvent> soundEvent = q == null ? null : q.getEquipSound();
            if (soundEvent != null) {
                entity.emitGameEvent(GameEvent.EQUIP);
                entity.playSound(soundEvent.value(), 1, 1);
            }
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (stack.getItem() instanceof ItemTracker.Trackable t) {
            Living<?> l = Living.living(entity);
            t.onUnequipped(l, l.getArmour().forceRemove(t));
        }
        Equipment q = Equipment.fromStack(stack);
        RegistryEntry<SoundEvent> soundEvent = q == null ? null : q.getEquipSound();
        if (soundEvent != null) {
            entity.emitGameEvent(GameEvent.EQUIP);
            entity.playSound(soundEvent.value(), 1, 1);
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
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        item.inventoryTick(stack, entity.getWorld(), entity, slot.index(), false);
    }

    @Override
    public Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, Identifier slotIdentifier) {
        Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> modifiers = Trinket.super.getModifiers(stack, slot, entity, slotIdentifier);

        if (item instanceof WearableItem wearable) {
            EquipmentSlot es = wearable.getSlotType(stack);
            stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().forEach(entry -> {
                if (entry.slot().matches(es)) {
                    modifiers.put(entry.attribute(), entry.modifier());
                }
            });
        }
        return modifiers;
    }
}
