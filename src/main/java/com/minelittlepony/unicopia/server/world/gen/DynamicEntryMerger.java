package com.minelittlepony.unicopia.server.world.gen;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.util.Identifier;

public class DynamicEntryMerger<T> implements RegistryEntryAddedCallback<T> {

    private final String sourceNamespace;

    private final Map<Identifier, SourceTargetPair> unfilledEntries = new HashMap<>();

    private final BiConsumer<T, T> combiner;

    public DynamicEntryMerger(String sourceNamespace, BiConsumer<T, T> combiner) {
        this.sourceNamespace = sourceNamespace;
        this.combiner = combiner;
    }

    @Override
    public void onEntryAdded(int rawId, Identifier id, T object) {
        boolean isInjected = id.getNamespace().equals(sourceNamespace);
        if (isInjected || id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            Identifier targetId = isInjected ? Identifier.of(id.getPath()) : id;

            unfilledEntries.compute(targetId, (iid, pair) -> {
                pair = pair == null ? new SourceTargetPair() : pair;
                if (!pair.offer(isInjected, object)) {
                    return pair;
                }
                combiner.accept(pair.source, pair.target);
                return null;
            });
        }
    }

    private class SourceTargetPair {
        @Nullable
        public T source;
        @Nullable
        public T target;

        public boolean offer(boolean isSource, T t) {
            if (isSource) {
                source = t;
            } else {
                target = t;
            }
            return source != null && target != null;
        }
    }
}
