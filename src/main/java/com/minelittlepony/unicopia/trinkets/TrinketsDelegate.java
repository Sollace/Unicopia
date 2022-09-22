package com.minelittlepony.unicopia.trinkets;

import java.util.*;
import java.util.stream.Stream;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public interface TrinketsDelegate {
    Identifier MAINHAND = new Identifier("hand:glove");
    Identifier OFFHAND = new Identifier("offhand:glove");
    Identifier NECKLACE = new Identifier("chest:necklace");

    Set<Identifier> ALL = new TreeSet<>(List.of(MAINHAND, OFFHAND, NECKLACE));

    TrinketsDelegate EMPTY = new TrinketsDelegate() {};

    static TrinketsDelegate getInstance() {
        if (!hasTrinkets()) {
            return EMPTY;
        }

        return TrinketsDelegateImpl.INSTANCE;
    }

    static boolean hasTrinkets() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    default boolean equipStack(LivingEntity entity, Identifier slot, ItemStack stack) {
        EquipmentSlot eq = MobEntity.getPreferredEquipmentSlot(stack);
        if (!entity.getEquippedStack(eq).isEmpty()) {
            return false;
        }

        entity.equipStack(eq, stack.split(1));
        if (entity instanceof MobEntity mob) {
            mob.setEquipmentDropChance(eq, 2.0f);
            mob.setPersistent();
        }
        return true;
    }

    default Set<Identifier> getAvailableTrinketSlots(LivingEntity entity, Set<Identifier> probedSlots) {
        return Set.of();
    }

    default Stream<ItemStack> getEquipped(LivingEntity entity) {
        return Stream.empty();
    }

    default Stream<ItemStack> getEquipped(LivingEntity entity, Identifier slot) {

        if (slot == NECKLACE || slot == MAINHAND) {
            return Stream.of(entity.getEquippedStack(EquipmentSlot.CHEST));
        }
        if (slot == OFFHAND) {
            return Stream.of(entity.getOffHandStack());
        }

        return Stream.empty();
    }

    default void registerTrinket(Item item) {

    }

    default boolean isTrinketSlot(Slot slot) {
        return false;
    }
}
