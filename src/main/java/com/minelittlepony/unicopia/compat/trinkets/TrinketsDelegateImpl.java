package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.item.enchantment.EnchantmentUtil;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgTrinketBroken;
import com.minelittlepony.unicopia.util.InventoryUtil;
import dev.emi.trinkets.TrinketSlot;
import dev.emi.trinkets.api.*;
import dev.emi.trinkets.api.TrinketEnums.DropRule;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;

public class TrinketsDelegateImpl implements TrinketsDelegate {
    public static final TrinketsDelegateImpl INSTANCE = new TrinketsDelegateImpl();

    // who tf designed this api?

    @Override
    public void bootstrap() {
        TrinketDropCallback.EVENT.register((rule, stack, ref, entity) -> {
            if (EnchantmentUtil.getLevel(UEnchantments.HEART_BOUND, stack) > 0) {
                return DropRule.KEEP;
            }
            return rule;
        });
    }

    @Override
    public boolean equipStack(LivingEntity entity, Identifier slot, ItemStack stack) {
        return getInventory(entity, slot).map(inventory -> {
            for (int position = 0; position < inventory.size(); position++) {
                if (inventory.getStack(position).isEmpty() && TrinketSlot.canInsert(stack, new SlotReference(inventory, position), entity)) {

                    Equipment q = Equipment.fromStack(stack);
                    RegistryEntry<SoundEvent> soundEvent = q == null ? null : q.getEquipSound();
                    inventory.setStack(position, stack.split(1));
                    if (soundEvent != null) {
                       entity.emitGameEvent(GameEvent.EQUIP);
                       entity.playSound(soundEvent.value(), 1, 1);
                    }
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    @Override
    public void setEquippedStack(LivingEntity entity, Identifier slot, ItemStack stack) {
        getInventory(entity, slot).ifPresent(inventory -> {
            Equipment q = Equipment.fromStack(stack);
            RegistryEntry<SoundEvent> soundEvent = q == null ? null : q.getEquipSound();
            inventory.clear();
            inventory.setStack(0, stack);
            if (soundEvent != null) {
                entity.emitGameEvent(GameEvent.EQUIP);
                entity.playSound(soundEvent.value(), 1, 1);
            }
        });
    }

    @Override
    public Set<Identifier> getAvailableTrinketSlots(LivingEntity entity, Set<Identifier> probedSlots) {
        probedSlots = new HashSet<>(probedSlots);
        probedSlots.removeAll(getInventories(entity)
                .filter(inventory -> InventoryUtil.getOpenSlot(inventory) == -1)
                .map(slot -> slot.getSlotType())
                .map(TrinketsDelegateImpl::getSlotId)
                .collect(Collectors.toSet()));
        return probedSlots;
    }

    @Override
    public Stream<EquippedStack> getEquipped(LivingEntity entity, Identifier slot, @Nullable Predicate<ItemStack> predicate) {
        return getInventory(entity, slot).stream().flatMap(inventory -> {
            return InventoryUtil.stream(inventory).filter(s -> !s.isEmpty() && (predicate == null || predicate.test(s))).map(stack -> {
                ItemStack oldStack = stack.copy();
                return new EquippedStack(stack, inventory::markUpdate, l -> {
                    inventory.markUpdate();
                    Channel.SERVER_TRINKET_BROKEN.sendToSurroundingPlayers(new MsgTrinketBroken(oldStack, entity.getId()), entity);
                });
            });
        });
    }

    @Override
    public void registerTrinket(Item item) {
        TrinketsApi.registerTrinket(item, new UnicopiaTrinket(item));
    }

    private Optional<TrinketComponent> getTrinketComponent(LivingEntity entity) {
        try {
            return TrinketsApi.getTrinketComponent(entity);
        } catch (Throwable ingnored) {}
        return Optional.empty();
    }

    public Optional<TrinketInventory> getInventory(LivingEntity entity, Identifier slot) {
        return getTrinketComponent(entity)
                .map(component -> component.getInventory()
                .getOrDefault(slot.getNamespace(), Map.of())
                .getOrDefault(slot.getPath(), null)
        );
    }

    public Stream<TrinketInventory> getInventories(LivingEntity entity) {
        return getTrinketComponent(entity)
                .stream()
                .map(component -> component.getInventory())
                .flatMap(groups -> groups.values().stream())
                .flatMap(group -> group.values().stream());
    }

    public Optional<SlotGroup> getGroup(LivingEntity entity, Identifier slotId) {
        return getTrinketComponent(entity)
                .stream()
                .map(component -> component.getGroups().get(slotId.getNamespace()))
                .findFirst();
    }

    @Override
    public Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, Identifier slotId, int i, int x, int y) {
        return getGroup(entity, slotId).flatMap(group -> {
            return getInventory(entity, slotId).map(inventory -> {
                return new SpellbookTrinketSlot(handler, inventory, i, x, y, group);
            });
        });
    }

    @Override
    public boolean isTrinketSlot(Slot slot) {
        return slot instanceof TrinketSlot || slot instanceof SpellbookTrinketSlot;
    }

    private static Identifier getSlotId(SlotType slotType) {
        return Identifier.of(slotType.getGroup(), slotType.getName());
    }

    public static int getMaxCount(ItemStack stack, SlotReference ref, int normal) {
        Trinket trinket = TrinketsApi.getTrinket(stack.getItem());
        if (trinket instanceof UnicopiaTrinket ut) {
            return Math.min(
                    normal,
                    Math.min(
                            stack.getMaxCount(),
                            ut.getMaxCount(stack, ref)
            ));
        }
        return normal;
    }

    public static boolean tryInsert(TrinketInventory inv, ItemStack stack, PlayerEntity user) {
        int i = InventoryUtil.getOpenSlot(inv);
        if (i == -1) {
            return false;
        }

        SlotReference ref = new SlotReference(inv, i);
        if (!TrinketSlot.canInsert(stack, ref, user)) {
            return false;
        }

        Trinket trinket = TrinketsApi.getTrinket(stack.getItem());

        Equipment q = Equipment.fromStack(stack);
        RegistryEntry<SoundEvent> soundEvent = q == null ? null : q.getEquipSound();
        inv.setStack(i, stack.split(trinket instanceof UnicopiaTrinket ut ? ut.getMaxCount(stack, ref) : stack.getMaxCount()));
        if (!stack.isEmpty() && soundEvent != null) {
            user.emitGameEvent(GameEvent.EQUIP);
            user.playSound(soundEvent.value(), 1, 1);
        }

        return true;
    }
}
