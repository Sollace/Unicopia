package com.minelittlepony.unicopia.entity;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemTracker implements NbtSerialisable {
    public static final long TICKS = 1;
    public static final long SECONDS = 20 * TICKS;
    public static final long HOURS = 1000 * TICKS;
    public static final long DAYS = 24 * HOURS;

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

    public void update(Living<?> living, Stream<ItemStack> stacks) {
        final Set<Trackable> found = new HashSet<>();
        final Set<ItemStack> foundStacks = new HashSet<>();
        stacks.forEach(stack -> {
            if (stack.getItem() instanceof Trackable trackable) {
                items.compute(trackable, (item, prev) -> prev == null ? 1 : prev + 1);
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

        if (!(living.getEntity() instanceof PlayerEntity)) {
            foundStacks.forEach(stack -> {
                if (getTicks((Trackable)stack.getItem()) == 1) {
                    stack.inventoryTick(living.getReferenceWorld(), living.getEntity(), 0, false);
                }
            });
        }
    }

    public long getTicks(Trackable charm) {
        return items.getOrDefault(charm.asItem(), 0L);
    }

    public boolean contains(Trackable charm) {
        return getTicks(charm) > 0;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        items.forEach((charm, count) -> {
            compound.putLong(Registry.ITEM.getId(charm.asItem()).toString(), count);
        });
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        items.clear();
        compound.getKeys().stream().map(Identifier::tryParse)
            .filter(Objects::nonNull)
            .map(id -> Map.entry(Registry.ITEM.get(id), compound.getLong(id.toString())))
            .filter(i -> i.getKey() instanceof Trackable && i.getValue() > 0)
            .forEach(item -> items.put((Trackable)item.getKey(), item.getValue()));
    }

    public interface Trackable extends ItemConvertible {
        void onUnequipped(Living<?> living, long timeWorn);

        void onEquipped(Living<?> living);
    }
}