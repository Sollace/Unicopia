package com.minelittlepony.unicopia.entity.behaviour;

import java.util.*;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.util.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public record Inventory (
        Map<EquipmentSlot, ItemStack> equipment,
        Optional<DefaultedList<ItemStack>> mainInventory
    ) {
    public static final Codec<Inventory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.CODEC).fieldOf("equipment").forGetter(Inventory::equipment),
            CodecUtils.defaultedList(ItemStack.CODEC, ItemStack.EMPTY).optionalFieldOf("main").forGetter(Inventory::mainInventory)
    ).apply(instance, Inventory::new));

    public static Optional<Inventory> of(LivingEntity entity) {
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        for (var slot : EquipmentSlot.values()) {
            equipment.put(slot, entity.getEquippedStack(slot));
        }

        if (entity instanceof PlayerEntity player) {
            PlayerInventory inventory = player.getInventory();
            DefaultedList<ItemStack> mainInventory = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
            for (int i = 0; i < mainInventory.size(); i++) {
                mainInventory.set(i, inventory.getStack(i));
            }

            return Optional.of(new Inventory(equipment, Optional.of(mainInventory)));
        }

        return Optional.of(new Inventory(equipment, Optional.empty()));
    }

    /**
     * Copies the inventory into another entity.
     *
     * @return Returns the left overs that could not be copied
     */
    public Inventory copyInto(LivingEntity into) {
        if (into instanceof PlayerEntity player) {
            mainInventory().ifPresentOrElse(main -> {
                PlayerInventory pe = player.getInventory();
                int i = 0;
                for (; i < pe.size(); i++) {
                    pe.setStack(i, i < main.size() ? main.get(i) : ItemStack.EMPTY);
                }
                for (; i < main.size(); i++) {
                    into.dropStack(main.get(i));
                }
            }, () -> {
                PlayerInventory pe = player.getInventory();
                for (int i = 0; i < pe.size(); i++) {
                    pe.setStack(i, ItemStack.EMPTY);
                }
                equipment().forEach(player::equipStack);
            });

            return null;
        }

        equipment().forEach(into::equipStack);
        return this;
    }

    public static void swapInventories(LivingEntity me, Optional<Inventory> myInv, LivingEntity them, Optional<Inventory> theirInv,
            Consumer<Inventory> outOverflowConsumer,
            Consumer<Inventory> inOverflowConsumer) {
        Optional<Inventory> outOverflow = Inventory.copyInventoryInto(myInv, them);
        Optional<Inventory> inOverflow = Inventory.copyInventoryInto(theirInv, me);

        outOverflow.ifPresent(outOverflowConsumer);
        inOverflow.ifPresent(inOverflowConsumer);
    }

    public static Optional<Inventory> copyInventoryInto(Optional<Inventory> inventory, LivingEntity to) {
        return inventory.map(inv -> inv.copyInto(to));
    }
}