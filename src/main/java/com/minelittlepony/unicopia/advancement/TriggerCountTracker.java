package com.minelittlepony.unicopia.advancement;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.Copyable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TriggerCountTracker implements Copyable<TriggerCountTracker> {
    public static final Codec<TriggerCountTracker> CODEC = Codec.unboundedMap(Key.CODEC, Codec.INT).xmap(TriggerCountTracker::new, tracker -> tracker.entries);

    private final Object2IntMap<Key> entries = new Object2IntOpenHashMap<>();

    public TriggerCountTracker(Map<Key, Integer> entries) {
        this.entries.putAll(entries);
    }

    public int update(AdvancementEntry advancement, String criterionName) {
        return entries.computeInt(new Key(advancement.id(), criterionName), (key, initial) -> (initial == null ? 0 : initial) + 1);
    }

    public void removeGranted(ServerPlayerEntity player, PlayerAdvancementTracker tracker) {
        entries.keySet().removeIf(key -> {
            @Nullable
            AdvancementEntry a = player.getServer().getAdvancementLoader().get(key.advancement());
            return a == null || tracker.getProgress(a).isDone();
        });
    }

    @Override
    public void copyFrom(TriggerCountTracker other, boolean alive) {
        entries.clear();
        entries.putAll(other.entries);
    }

    record Key(Identifier advancement, String criterion) {
        public static final Codec<Key> CODEC = Codec.STRING.flatXmap(s -> {
           String[] parts = s.split(":");
           return parts.length == 3
                   ? DataResult.success(new Key(Identifier.of(parts[0], parts[1]), parts[2]))
                   : DataResult.error(() -> "String '" + s + "' was in the wrong format");
        }, key -> DataResult.success(key.toString()));

        @Override
        public String toString() {
            return advancement.toString() + ":" + criterion;
        }
    }

}
