package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler;

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
    Identifier FACE = new Identifier("head:face");

    Set<Identifier> ALL = new TreeSet<>(List.of(MAINHAND, OFFHAND, NECKLACE, FACE));

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

    default void bootstrap() {

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

    default void setEquippedStack(LivingEntity entity, Identifier slot, ItemStack stack) {
        EquipmentSlot eq = slot == FACE ? EquipmentSlot.HEAD
                : slot == NECKLACE ? EquipmentSlot.CHEST
                : slot == MAINHAND ? EquipmentSlot.CHEST
                : slot == OFFHAND ? EquipmentSlot.OFFHAND
                : null;
        if (eq != null) {
            entity.equipStack(eq, stack);
        }
    }

    default Set<Identifier> getAvailableTrinketSlots(LivingEntity entity, Set<Identifier> probedSlots) {
        return probedSlots.stream().filter(slot -> getEquipped(entity, slot).anyMatch(ItemStack::isEmpty)).collect(Collectors.toSet());
    }

    default Stream<ItemStack> getEquipped(LivingEntity entity, Identifier slot) {

        if (slot == FACE) {
            return Stream.of(entity.getEquippedStack(EquipmentSlot.HEAD));
        }
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

    default Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, Identifier slotId, int i, int x, int y) {
        return Optional.empty();
    }

    default boolean isTrinketSlot(Slot slot) {
        return false;
    }

    interface Inventory extends EntityConvertable<LivingEntity> {

        default Stream<ItemStack> getEquippedStacks(Identifier slot) {
            return TrinketsDelegate.getInstance().getEquipped(asEntity(), slot);
        }

        default ItemStack getEquippedStack(Identifier slot) {
            return getEquippedStacks(slot).findFirst().orElse(ItemStack.EMPTY);
        }

        default void equipStack(Identifier slot, ItemStack stack) {
            TrinketsDelegate.getInstance().setEquippedStack(asEntity(), slot, stack);
        }
    }
}
