package com.minelittlepony.unicopia.advancements;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;

public abstract class AbstractTrigger<E extends AbstractTrigger.Entry<T>, T extends AbstractCriterionInstance> implements ICriterionTrigger<T> {

    protected final Map<PlayerAdvancements, E> listeners = Maps.newHashMap();

    @Override
    public void addListener(PlayerAdvancements key, Listener<T> listener) {
        listeners.computeIfAbsent(key, this::createEntry).listeners.add(listener);;
    }

    @Override
    public void removeListener(PlayerAdvancements key, Listener<T> listener) {
        if (listeners.containsKey(key)) {
            E entry = listeners.get(key);

            entry.listeners.remove(listener);
            if (entry.listeners.isEmpty()) {
                listeners.remove(key);
            }
        }
    }

    @Override
    public void removeAllListeners(PlayerAdvancements key) {
        if (listeners.containsKey(key)) {
            listeners.remove(key);
        }
    }

    protected abstract E createEntry(PlayerAdvancements advancement);

    protected static class Entry<T extends AbstractCriterionInstance> {
        protected final PlayerAdvancements advancement;

        protected final List<Listener<T>> listeners = Lists.newArrayList();

        Entry(PlayerAdvancements key) {
            advancement = key;
        }
    }
}
