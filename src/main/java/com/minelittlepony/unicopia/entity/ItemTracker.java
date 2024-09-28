package com.minelittlepony.unicopia.entity;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.compat.trinkets.TrinketsDelegate;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.*;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class ItemTracker implements NbtSerialisable, Copyable<ItemTracker>, Tickable, TrinketsDelegate.Inventory {
    public static final long TICKS = 1;
    public static final long SECONDS = 20 * TICKS;
    public static final long HOURS = 1000 * TICKS;
    public static final long DAYS = 24 * HOURS;

    public static String formatTicks(long ticks) {
        long days = ticks / (SECONDS * 60 * 24);
        ticks %= (SECONDS * 60 * 60 * 24);
        long hours = ticks / (SECONDS * 60 * 60);
        ticks %= (SECONDS * 60 * 60);
        long minutes = ticks / (SECONDS * 60);
        ticks %= (SECONDS * 60);
        long seconds = ticks / SECONDS;
        return String.format("%dd, %dh %dm %ds", days, hours, minutes, seconds);
    }

    private final Map<Trackable, Long> items = new HashMap<>();

    public static Predicate<LivingEntity> wearing(Trackable charm, Predicate<Long> range) {
        return e -> Living.getOrEmpty(e)
                    .map(Living::getArmour)
                    .map(a -> a.getTicks(charm))
                    .filter(range)
                    .isPresent();
    }

    public static Predicate<Long> between(long minTime, long maxTime) {
        return before(maxTime).and(after(minTime));
    }

    public static Predicate<Long> before(long maxTime) {
        return ticks -> ticks <= maxTime;
    }

    public static Predicate<Long> after(long maxTime) {
        return ticks -> ticks <= maxTime;
    }

    private final Living<?> living;

    public ItemTracker(Living<?> living) {
        this.living = living;
    }

    @Override
    public LivingEntity asEntity() {
        return living.asEntity();
    }

    @Override
    public void tick() {
        update(living.getArmourStacks());
    }

    private void update(Stream<ItemStack> stacks) {
        final Set<Trackable> found = new HashSet<>();
        final Set<ItemStack> foundStacks = new HashSet<>();

        stacks.forEach(stack -> {
            if (stack.getItem() instanceof Trackable trackable) {
                if (items.compute(trackable, (item, prev) -> prev == null ? 1 : prev + 1) == 1) {
                    trackable.onEquipped(this.living);
                }
                found.add(trackable);
                foundStacks.add(stack);
            }
        });

        items.entrySet().removeIf(e -> {
            if (!found.contains(e.getKey())) {
                e.getKey().onUnequipped(living, e.getValue());
                return true;
            }
            return false;
        });

        if (!(living instanceof Pony)) {
            foundStacks.forEach(stack -> {
                stack.inventoryTick(living.asWorld(), living.asEntity(), 0, false);
            });
        }
    }

    public long forceRemove(Trackable charm) {
        @Nullable Long time = items.remove(charm);
        return time == null ? 0 : time;
    }

    public long getTicks(Trackable charm) {
        return items.getOrDefault(charm.asItem(), 0L);
    }

    public boolean contains(Trackable charm) {
        return getTicks(charm) > 0;
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        items.forEach((charm, count) -> {
            compound.putLong(Registries.ITEM.getId(charm.asItem()).toString(), count);
        });
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        items.clear();
        compound.getKeys().stream().map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .map(id -> Map.entry(Registries.ITEM.get(id), compound.getLong(id.toString())))
            .filter(i -> i.getKey() instanceof Trackable && i.getValue() > 0)
            .forEach(item -> items.put((Trackable)item.getKey(), item.getValue()));
    }

    @Override
    public void copyFrom(ItemTracker other, boolean alive) {
        items.clear();
        if (alive) {
            items.putAll(other.items);
        }
    }

    public interface Trackable extends ItemConvertible {
        void onUnequipped(Living<?> living, long timeWorn);

        void onEquipped(Living<?> living);
    }
}