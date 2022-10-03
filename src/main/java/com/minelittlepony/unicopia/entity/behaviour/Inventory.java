package com.minelittlepony.unicopia.entity.behaviour;

import java.util.*;
import java.util.function.Consumer;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.collection.DefaultedList;

public record Inventory (
        Map<EquipmentSlot, ItemStack> equipment,
        Optional<DefaultedList<ItemStack>> mainInventory
    ) {

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

    public NbtCompound toNBT(NbtCompound compound) {
        NbtCompound eq = new NbtCompound();
        equipment().forEach((slot, stack) -> {
            eq.put(slot.getName(), stack.writeNbt(new NbtCompound()));
        });
        compound.put("equipment", eq);
        mainInventory().ifPresent(main -> {
            NbtList list = new NbtList();
            main.forEach(stack -> {
                list.add(stack.writeNbt(new NbtCompound()));
            });
            compound.put("main", list);
        });
        return compound;
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

    public static Inventory fromNBT(NbtCompound compound) {
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);
        NbtCompound eq = compound.getCompound("equipment");
        eq.getKeys().forEach(key -> {
            equipment.put(EquipmentSlot.byName(key), ItemStack.fromNbt(eq.getCompound(key)));
        });

        if (!compound.contains("main", NbtElement.LIST_TYPE)) {
            return new Inventory(equipment, Optional.empty());
        }

        NbtList list = compound.getList("main", NbtElement.COMPOUND_TYPE);
        DefaultedList<ItemStack> main = DefaultedList.ofSize(list.size(), ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            main.set(i, ItemStack.fromNbt(list.getCompound(i)));
        }
        return new Inventory(equipment, Optional.of(main));
    }
}