package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EntityConvertable;
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
import net.minecraft.util.Identifier;

public interface TrinketsDelegate {
    Identifier MAIN_GLOVE = new Identifier("hand:glove");
    Identifier SECONDARY_GLOVE = new Identifier("offhand:glove");
    Identifier NECKLACE = new Identifier("chest:necklace");
    Identifier FACE = new Identifier("head:face");

    Set<Identifier> ALL = new TreeSet<>(List.of(MAIN_GLOVE, SECONDARY_GLOVE, NECKLACE, FACE));

    TrinketsDelegate EMPTY = new TrinketsDelegate() {};

    static TrinketsDelegate getInstance(@Nullable LivingEntity entity) {
        if (!hasTrinkets() || (entity != null && !(entity instanceof PlayerEntity))) {
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
                : slot == MAIN_GLOVE ? EquipmentSlot.CHEST
                : slot == SECONDARY_GLOVE ? EquipmentSlot.OFFHAND
                : null;
        if (eq != null) {
            entity.equipStack(eq, stack);
        }
    }

    default Set<Identifier> getAvailableTrinketSlots(LivingEntity entity, Set<Identifier> probedSlots) {
        return probedSlots.stream().filter(slot -> getEquipped(entity, slot).map(EquippedStack::stack).anyMatch(ItemStack::isEmpty)).collect(Collectors.toSet());
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, Identifier slot, TagKey<Item> tag) {
        return getEquipped(entity, slot, stack -> stack.isIn(tag));
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, Identifier slot) {
        return getEquipped(entity, slot, (Predicate<ItemStack>)null);
    }

    default Stream<EquippedStack> getEquipped(LivingEntity entity, Identifier slot, @Nullable Predicate<ItemStack> predicate) {

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

    default Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, Identifier slotId, int i, int x, int y) {
        return Optional.empty();
    }

    default boolean isTrinketSlot(Slot slot) {
        return false;
    }

    interface Inventory extends EntityConvertable<LivingEntity> {

        default Stream<EquippedStack> getEquippedStacks(Identifier slot) {
            return TrinketsDelegate.getInstance(asEntity()).getEquipped(asEntity(), slot);
        }

        default EquippedStack getEquippedStack(Identifier slot) {
            return getEquippedStacks(slot).findFirst().orElse(EquippedStack.EMPTY);
        }

        default void equipStack(Identifier slot, ItemStack stack) {
            TrinketsDelegate.getInstance(asEntity()).setEquippedStack(asEntity(), slot, stack);
        }
    }

    record EquippedStack(ItemStack stack, Runnable sendUpdate, Consumer<LivingEntity> breakStatusSender) {
        public static final EquippedStack EMPTY = new EquippedStack(ItemStack.EMPTY, () -> {}, l -> {});

        EquippedStack(LivingEntity entity, EquipmentSlot slot) {
            this(entity.getEquippedStack(slot), () -> {}, l -> l.sendEquipmentBreakStatus(slot));
        }
    }
}
