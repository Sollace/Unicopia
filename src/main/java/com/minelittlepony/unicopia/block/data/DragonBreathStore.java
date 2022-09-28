package com.minelittlepony.unicopia.block.data;

import java.util.*;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class DragonBreathStore extends PersistentState {
    private static final long PURGE_INTERVAL = 1000 * 60 * 60; // 1 hour
    private static final long MAX_MESSAGE_HOLD_TIME = PURGE_INTERVAL * 24; // 24 hours
    private static final Identifier ID = Unicopia.id("dragon_breath");

    public static DragonBreathStore get(World world) {
        return WorldOverlay.getPersistableStorage(world, ID, DragonBreathStore::new, DragonBreathStore::new);
    }

    private final Map<String, List<Entry>> payloads = new HashMap<>();

    private final Object locker = new Object();

    DragonBreathStore(World world, NbtCompound compound) {
        this(world);
        compound.getKeys().forEach(key -> {
            compound.getList(key, NbtElement.COMPOUND_TYPE).forEach(entry -> {
                put(key, new Entry((NbtCompound)entry));
            });
        });
    }

    DragonBreathStore(World world) {

    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        synchronized (locker) {
            payloads.forEach((id, uuids) -> {
                NbtList list = new NbtList();
                uuids.forEach(entry -> {
                    if (entry.created < System.currentTimeMillis() - MAX_MESSAGE_HOLD_TIME) {
                        list.add(entry.toNBT(new NbtCompound()));
                    }
                });
                compound.put(id, list);
            });

            return compound;
        }
    }

    public List<Entry> popEntries(String recipient) {
        synchronized (locker) {
            List<Entry> entries = doPurge().get(recipient);
            if (entries == null) {
                return List.of();
            }

            long now = System.currentTimeMillis();
            List<Entry> collected = new ArrayList<>();
            entries.removeIf(entry -> {
                if (entry.created < now - 1000) {
                    collected.add(entry);
                    return true;
                }
                return false;
            });
            return collected;
        }
    }

    public List<Entry> peekEntries(String recipient) {
        synchronized (locker) {
            return doPurge().getOrDefault(recipient, List.of());
        }
    }

    public void put(String recipient, ItemStack payload) {

        if (payload.getItem() == UItems.OATS) {
            ItemStack oats = UItems.IMPORTED_OATS.getDefaultStack();
            oats.setNbt(payload.getNbt());
            oats.setCount(payload.getCount());
            put(recipient, oats);
            return;
        }

        synchronized (locker) {
            doPurge();
            if (peekEntries(recipient).stream().noneMatch(i -> {
               if (ItemStack.canCombine(i.payload(), payload)) {
                   int combinedCount = i.payload().getCount() + payload.getCount();
                   if (combinedCount <= i.payload().getMaxCount()) {
                       i.payload().setCount(combinedCount);
                       return true;
                   }
               }
               return false;
            })) {
                put(recipient, new Entry(System.currentTimeMillis() + (long)(Math.random() * 1999), payload));
            }
        }
    }

    private void put(String recipient, Entry entry) {
        payloads.computeIfAbsent(recipient, id -> new ArrayList<>()).add(entry);
    }

    private Map<String, List<Entry>> doPurge() {
        long now = System.currentTimeMillis();
        if (now % PURGE_INTERVAL == 0) {
            payloads.entrySet().removeIf(entry -> {
               entry.getValue().removeIf(e -> e.created < now - MAX_MESSAGE_HOLD_TIME);
               return entry.getValue().isEmpty();
            });
        }
        return payloads;
    }

    public record Entry(
            long created,
            ItemStack payload) {

        public Entry(NbtCompound compound) {
            this(compound.getLong("created"), ItemStack.fromNbt(compound.getCompound("payload")));
        }

        public NbtCompound toNBT(NbtCompound compound) {
            compound.putLong("created", created);
            compound.put("payload", payload().writeNbt(new NbtCompound()));
            return compound;
        }
    }
}
