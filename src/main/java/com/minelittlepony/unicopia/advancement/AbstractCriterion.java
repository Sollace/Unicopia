package com.minelittlepony.unicopia.advancement;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.advancement.criterion.CriterionConditions;

abstract class AbstractCriterion<E extends AbstractCriterion.Entry<T>, T extends CriterionConditions> implements Criterion<T> {

    protected final Map<PlayerAdvancementTracker, E> listeners = Maps.newHashMap();

    @Override
    public void beginTrackingCondition(PlayerAdvancementTracker key, Criterion.ConditionsContainer<T> listener) {
        listeners.computeIfAbsent(key, this::createEntry).listeners.add(listener);
    }

    @Override
    public void endTrackingCondition(PlayerAdvancementTracker key, Criterion.ConditionsContainer<T> listener) {
        if (listeners.containsKey(key)) {
            E entry = listeners.get(key);

            entry.listeners.remove(listener);
            if (entry.listeners.isEmpty()) {
                listeners.remove(key);
            }
        }
    }

    @Override
    public void endTracking(PlayerAdvancementTracker key) {
        if (listeners.containsKey(key)) {
            listeners.remove(key);
        }
    }

    protected abstract E createEntry(PlayerAdvancementTracker advancement);

    protected static class Entry<T extends CriterionConditions> {
        protected final PlayerAdvancementTracker advancement;

        protected final List<Criterion.ConditionsContainer<T>> listeners = Lists.newArrayList();

        Entry(PlayerAdvancementTracker key) {
            advancement = key;
        }
    }
}
