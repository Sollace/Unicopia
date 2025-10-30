package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.slot.Slot;

public interface TrinketsDelegate {
    SlotKey MAIN_GLOVE = SlotKey.of("hand", "glove");
    SlotKey SECONDARY_GLOVE = SlotKey.of("offhand", "glove");
    SlotKey NECKLACE = SlotKey.of("chest", "necklace");
    SlotKey FACE = SlotKey.of("head", "face");

    Set<SlotKey> ALL = new TreeSet<>(List.of(MAIN_GLOVE, SECONDARY_GLOVE, NECKLACE, FACE));

    TrinketsDelegate EMPTY = new TrinketsDelegate() {};

    static TrinketsDelegate getInstance(@Nullable LivingEntity entity) {
        if (!hasTrinkets() || (entity != null && !(entity instanceof PlayerEntity))) {
            return EMPTY;
        }
        return TrinketsDelegateImpl.INSTANCE;
    }

    static boolean hasTrinkets() {
        return FabricLoader.getInstance().isModLoaded("accessories");
    }

    default void bootstrap() {

    }

    default boolean equipStack(LivingEntity entity, ItemStack stack) {
        EquipmentSlot eq = entity.getPreferredEquipmentSlot(stack);
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

    default Stream<EquippedStack> getEquipped(LivingEntity entity, SlotKey slot, TagKey<Item> tag) {
        return getEquipped(entity, slot, stack -> stack.isIn(tag));
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, SlotKey slot) {
        return getEquipped(entity, slot, (Predicate<ItemStack>)null);
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, SlotKey slot, @Nullable Predicate<ItemStack> predicate) {

        if (slot == FACE && (predicate == null || predicate.test(entity.getEquippedStack(EquipmentSlot.HEAD)))) {
            return Stream.of(new EquippedStack(entity, EquipmentSlot.HEAD));
        }
        if ((slot == NECKLACE || slot == MAIN_GLOVE) && (predicate == null || predicate.test(entity.getEquippedStack(EquipmentSlot.CHEST)))) {
            return Stream.of(new EquippedStack(entity, EquipmentSlot.CHEST));
        }
        if (slot == SECONDARY_GLOVE && (predicate == null || predicate.test(entity.getEquippedStack(EquipmentSlot.OFFHAND)))) {
            return Stream.of(new EquippedStack(entity, EquipmentSlot.OFFHAND));
        }

        return Stream.empty();
    }

    default void registerTrinket(Item item) {

    }

    default Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, SlotKey slotId, int i, int x, int y) {
        return Optional.empty();
    }

    default boolean isTrinketSlot(Slot slot) {
        return false;
    }

    record EquippedStack(ItemStack stack, Runnable sendUpdate, Consumer<ItemStack> updater, Consumer<Item> breakStatusSender) {
        public static final EquippedStack EMPTY = new EquippedStack(ItemStack.EMPTY, () -> {}, s -> {}, l -> {});

        EquippedStack(LivingEntity entity, EquipmentSlot slot) {
            this(entity.getEquippedStack(slot), () -> {}, s -> entity.equipStack(slot, s), item -> entity.sendEquipmentBreakStatus(item, slot));
        }

        public void markChanged() {
            sendUpdate.run();
        }

        public void swap(ItemStack newStack) {
            updater.accept(newStack);
        }
    }
}
