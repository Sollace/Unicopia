package com.minelittlepony.unicopia.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.Lists;

public class Weighted<T> {

    private static final Random rand = new Random();

    private float totalWeight = 0;

    private final List<Entry> entries = Lists.newArrayList();

    public static <T> Weighted<T> of(Consumer<Weighted<T>> constructor) {
        Weighted<T> result = new Weighted<>();

        constructor.accept(result);

        return result;
    }

    public Weighted<T> put(int weight, @Nonnull T value) {
        entries.add(new Entry(weight, value));

        totalWeight += weight;

        recalculate();

        return this;
    }

    private void recalculate() {
        float rangeStart = 0;

        for (Entry i : entries) {
            i.min = rangeStart;
            i.max = rangeStart + (i.weight/totalWeight);

            rangeStart = i.max;
        }
    }

    public Optional<T> get() {
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        float random = rand.nextFloat();

        return entries.stream()
                .filter(i -> random >= i.min && random <= i.max)
                .map(Entry::getResult)
                .findFirst();
    }

    @Immutable
    class Entry {

        final float weight;

        final T result;

        float min;
        float max;

        Entry(int weight, T result) {
            this.weight = weight;
            this.result = result;
        }

        T getResult() {
            return result;
        }
    }
}
