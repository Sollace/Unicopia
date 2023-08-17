package com.minelittlepony.unicopia.trinkets;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.util.InventoryUtil;
import dev.emi.trinkets.TrinketSlot;
import dev.emi.trinkets.api.*;
import dev.emi.trinkets.api.TrinketEnums.DropRule;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
            if (EnchantmentHelper.getLevel(UEnchantments.HEART_BOUND, stack) > 0) {
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
                    SoundEvent soundEvent = stack.getItem() instanceof Equipment q ? q.getEquipSound() : null;
                    inventory.setStack(position, stack.split(1));
                    if (soundEvent != null) {
                       entity.emitGameEvent(GameEvent.EQUIP);
                       entity.playSound(soundEvent, 1, 1);
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
            SoundEvent soundEvent = stack.getItem() instanceof Equipment q ? q.getEquipSound() : null;
            inventory.clear();
            inventory.setStack(0, stack);
            if (soundEvent != null) {
                entity.emitGameEvent(GameEvent.EQUIP);
                entity.playSound(soundEvent, 1, 1);
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
    public Stream<ItemStack> getEquipped(LivingEntity entity, Identifier slot) {
        return getInventory(entity, slot).stream().flatMap(InventoryUtil::stream).filter(s -> !s.isEmpty());
    }

    @Override
    public void registerTrinket(Item item) {
        TrinketsApi.registerTrinket(item, new UnicopiaTrinket(item));
    }

    public Optional<TrinketInventory> getInventory(LivingEntity entity, Identifier slot) {
        return TrinketsApi.getTrinketComponent(entity)
                .map(component -> component.getInventory()
                .getOrDefault(slot.getNamespace(), Map.of())
                .getOrDefault(slot.getPath(), null)
        );
    }

    public Stream<TrinketInventory> getInventories(LivingEntity entity) {
        return TrinketsApi.getTrinketComponent(entity)
                .stream()
                .map(component -> component.getInventory())
                .flatMap(groups -> groups.values().stream())
                .flatMap(group -> group.values().stream());
    }

    public Optional<SlotGroup> getGroup(LivingEntity entity, Identifier slotId) {
        return TrinketsApi.getTrinketComponent(entity)
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
        return new Identifier(slotType.getGroup(), slotType.getName());
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

        SoundEvent soundEvent = stack.getItem() instanceof Equipment q ? q.getEquipSound() : null;
        inv.setStack(i, stack.split(trinket instanceof UnicopiaTrinket ut ? ut.getMaxCount(stack, ref) : stack.getMaxCount()));
        if (!stack.isEmpty() && soundEvent != null) {
            user.emitGameEvent(GameEvent.EQUIP);
            user.playSound(soundEvent, 1, 1);
        }

        return true;
    }
}
