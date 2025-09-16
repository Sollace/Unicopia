package com.minelittlepony.unicopia.compat.trinkets;

import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.item.FriendshipBraceletItem;
import com.minelittlepony.unicopia.item.WearableItem;
import com.minelittlepony.unicopia.item.component.Issuer;

import io.wispforest.accessories.api.Accessory;
import io.wispforest.accessories.api.attributes.AccessoryAttributeBuilder;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.event.GameEvent;

public class UnicopiaTrinket implements Accessory {

    private final Item item;

    public UnicopiaTrinket(Item item) {
        this.item = item;
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot) {
        if (slot.entity().isSpectator() || stack.isEmpty()) {
            return;
        }

        if (!(stack.getItem() instanceof ItemTracker.Trackable)) {
            EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
            RegistryEntry<SoundEvent> soundEvent = equippable == null ? null : equippable.equipSound();
            if (soundEvent != null) {
                slot.entity().emitGameEvent(GameEvent.EQUIP);
                slot.entity().playSound(soundEvent.value(), 1, 1);
            }
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot) {
        if (stack.getItem() instanceof ItemTracker.Trackable t) {
            Living<?> l = Living.living(slot.entity());
            t.onUnequipped(l, l.getArmour().forceRemove(t));
        }
        EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);
        RegistryEntry<SoundEvent> soundEvent = equippable == null ? null : equippable.equipSound();
        if (soundEvent != null) {
            slot.entity().emitGameEvent(GameEvent.EQUIP);
            slot.entity().playSound(soundEvent.value(), 1, 1);
        }
    }

    @Override
    public int maxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot) {
        if (item instanceof FriendshipBraceletItem && !Issuer.isSigned(stack)) {
            return false;
        }

        return slot.getStack().isEmpty();
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot) {
        item.inventoryTick(stack, slot.entity().getWorld(), slot.entity(), slot.slot(), false);
    }

    @Override
    public void getDynamicModifiers(ItemStack stack, SlotReference slot, AccessoryAttributeBuilder builder) {
        Accessory.super.getDynamicModifiers(stack, slot, builder);

        if (item instanceof WearableItem wearable) {
            EquipmentSlot es = wearable.getSlotType(stack);
            stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().forEach(entry -> {
                if (entry.slot().matches(es)) {
                    builder.addStackable(entry.attribute(), entry.modifier());
                }
            });
        }
    }
}
