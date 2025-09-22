package com.minelittlepony.unicopia.compat.trinkets;

import java.util.*;
import java.util.function.Predicate;
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
    public boolean equipStack(LivingEntity entity, ItemStack stack) {
        return TrinketItem.equipItem(entity, stack);
    }

    @Override
    public Stream<EquippedStack> getEquipped(LivingEntity entity, SlotKey slot, @Nullable Predicate<ItemStack> predicate) {
        return getInventory(entity, slot).stream().flatMap(inventory -> {
            return InventoryUtil.slots(inventory)
                    .filter(s -> !inventory.getStack(s).isEmpty() && (predicate == null || predicate.test(inventory.getStack(s))))
                    .map(index -> {
                ItemStack oldStack = inventory.getStack(index).copy();
                return new EquippedStack(inventory.getStack(index), inventory::markUpdate, stack -> {
                    inventory.setStack(index, stack);
                    inventory.markUpdate();
                }, l -> {
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

    @Override
    public Optional<Slot> createSlot(SpellbookScreenHandler handler, LivingEntity entity, SlotKey slotId, int i, int x, int y) {
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

    private static Optional<TrinketComponent> getTrinketComponent(LivingEntity entity) {
        try {
            return TrinketsApi.getTrinketComponent(entity);
        } catch (Throwable ingnored) {}
        return Optional.empty();
    }

    private static Optional<TrinketInventory> getInventory(LivingEntity entity, SlotKey slot) {
        return getTrinketComponent(entity)
                .map(component -> component.getInventory()
                .getOrDefault(slot.group(), Map.of())
                .getOrDefault(slot.name(), null)
        );
    }

    private static Stream<TrinketInventory> getInventories(LivingEntity entity) {
        return getTrinketComponent(entity)
                .stream()
                .map(component -> component.getInventory())
                .flatMap(groups -> groups.values().stream())
                .flatMap(group -> group.values().stream());
    }

    private static Optional<SlotGroup> getGroup(LivingEntity entity, SlotKey slotId) {
        return getTrinketComponent(entity)
                .stream()
                .map(component -> component.getGroups().get(slotId.group()))
                .findFirst();
    }

    public static boolean equipItem(PlayerEntity user, ItemStack stack) {
        return getInventories(user)
                .filter(inv -> tryInsert(inv, stack, user))
                .findFirst()
                .isPresent();
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

    private static boolean tryInsert(TrinketInventory inv, ItemStack stack, PlayerEntity user) {
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
