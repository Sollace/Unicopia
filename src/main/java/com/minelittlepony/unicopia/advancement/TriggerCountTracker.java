package com.minelittlepony.unicopia.advancement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.util.Copyable;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class TriggerCountTracker implements Copyable<TriggerCountTracker> {
    public static final Codec<TriggerCountTracker> CODEC = Codec.unboundedMap(
        Identifier.CODEC,
        Codec.unboundedMap(Codec.STRING, Codec.INT).xmap(i -> (Object2IntMap<String>)new Object2IntOpenHashMap<>(i), Function.identity())
    ).xmap(TriggerCountTracker::new, tracker -> tracker.entries);

    private final Map<Identifier, Object2IntMap<String>> entries = new HashMap<>();

    public TriggerCountTracker(Map<Identifier, Object2IntMap<String>> entries) {
        this.entries.putAll(entries);
    }

    public int update(AdvancementEntry advancement, String criterionName) {
        return entries.computeIfAbsent(advancement.id(), id -> new Object2IntOpenHashMap<>()).computeInt(criterionName, (key, initial) -> (initial == null ? 0 : initial) + 1);
    }

    public void removeGranted(ServerPlayerEntity player, PlayerAdvancementTracker tracker) {
        entries.entrySet().removeIf(entry -> {
            @Nullable
            AdvancementEntry a = player.getServer().getAdvancementLoader().get(entry.getKey());
            return a == null || tracker.getProgress(a).isDone();
        });
    }

    @Override
    public void copyFrom(TriggerCountTracker other, boolean alive) {
        entries.clear();
        entries.putAll(other.entries);
    }
}
