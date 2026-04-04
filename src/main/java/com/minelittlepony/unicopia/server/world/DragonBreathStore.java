package com.minelittlepony.unicopia.server.world;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.component.ConversionComponent;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.WorldView;

public class DragonBreathStore extends PersistentState {
    private static final long PURGE_INTERVAL = 1000 * 60 * 60; // 1 hour
    private static final long MAX_MESSAGE_HOLD_TIME = PURGE_INTERVAL * 24; // 24 hours
    private static final long MIN_MESSAGE_HOLD_TIME = 1000; // 1 second
    private static final Identifier ID = Unicopia.id("dragon_breath");
    private static final Codec<DragonBreathStore> CODEC = Codec.unboundedMap(Codec.STRING,
                Entry.CODEC.listOf().xmap(l -> l.stream().filter(Entry::isValid).collect(Collectors.toList()), list -> list.stream().filter(Entry::canPersist).toList())
            )
            .xmap(DragonBreathStore::new, store -> store.payloads);
    private static final PersistentStateKey<DragonBreathStore> KEY = new PersistentStateKey<>(ID, CODEC, DragonBreathStore::new);

    public static DragonBreathStore get(WorldView world) {
        return KEY.get(world);
    }

    private final Map<String, List<Entry>> payloads = new HashMap<>();

    private final Object locker = new Object();

    private DragonBreathStore(Map<String, List<Entry>> payloads) {
        payloads.forEach((recipient, entries) -> {
            if (!entries.isEmpty()) {
                this.payloads.put(recipient, entries);
            }
        });
    }

    private DragonBreathStore() {

    }

    public static Stream<Pair<DragonBreathStore, Stream<Entry>>> popAll(MinecraftServer server, String recipient) {
        return StreamSupport.stream(server.getWorlds().spliterator(), false)
                .map(DragonBreathStore::get)
                .map(store -> new Pair<>(store, store.popEntries(recipient).stream()));
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
                if (entry.created < now - MIN_MESSAGE_HOLD_TIME) {
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
        ConversionComponent conversion = payload.get(UDataComponentTypes.ITEM_AFTER_DRAGON_BREATH);

        if (conversion != null) {
            var item = conversion.getItem();
            if (item.isPresent()) {
                payload = payload.withItem(item.get());
            }
        }

        var finalPayload = payload;

        synchronized (locker) {
            doPurge();
            if (peekEntries(recipient).stream().noneMatch(i -> {
               if (ItemStack.areItemsAndComponentsEqual(i.payload(), finalPayload)) {
                   int combinedCount = i.payload().getCount() + finalPayload.getCount();
                   if (combinedCount <= i.payload().getMaxCount()) {
                       i.payload().setCount(combinedCount);
                       return true;
                   }
               }
               return false;
            })) {
                put(recipient, new Entry(payload));
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

    public record Entry(long created, ItemStack payload) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("created").forGetter(Entry::created),
                ItemStack.CODEC.fieldOf("payload").forGetter(Entry::payload)
        ).apply(i, Entry::new));

        public Entry(ItemStack payload) {
            this(System.currentTimeMillis() + (long)(Math.random() * 1999), payload);
        }

        public boolean isValid() {
            return !payload.isEmpty();
        }

        public boolean canPersist() {
            return isValid() && created() > System.currentTimeMillis() - MAX_MESSAGE_HOLD_TIME;
        }
    }
}
