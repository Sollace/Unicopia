package com.minelittlepony.unicopia.util;

import java.util.*;
import java.util.function.*;

/**
 * Utility for performing an atomic swap of two values.
 */
@FunctionalInterface
public interface Swap<T> extends BiConsumer<T, T> {
    /**
     * Returns a new swap that performs the same action as this one only if the passed in shouldRun check passes on both inputs.
     */
    @SuppressWarnings("unchecked")
    default <E> Swap<E> upcast(Predicate<E> shouldRun) {
        Swap<T> swap = this;
        return (a, b) -> {
            if (shouldRun.test(a) && shouldRun.test(b)) {
                swap.accept((T)a, (T)b);
            }
        };
    }

    @SafeVarargs
    static <E> Swap<E> union(Swap<E>... swaps) {
        return union(List.of(swaps));
    }

    /**
     * Creates a swap from a collection of multiple swaps.
     * Executes them in the order they are presented by Iterable#forEach
     *
     * Changes to the underlying list an equivalent change to the swap returned by this method.
     */
    static <E> Swap<E> union(Iterable<Swap<E>> swaps) {
        return (a, b) -> swaps.forEach(consumer -> consumer.accept(a, b));
    }

    /**
     * Creates a swap for switching numerical values.
     */
    static <E, N extends Number> Swap<E> of(Function<E, N> getter, BiConsumer<E, N> setter, Function<Float, N> converter) {
        return of(getter, e -> converter.apply(1F), setter, converter);
    }

    /**
     * Creates a swap for converting numerical values where the source and destination may have different scales.
     */
    static <E, N extends Number> Swap<E> of(Function<E, N> getter, Function<E, N> maxGetter, BiConsumer<E, N> setter, Function<Float, N> converter) {
        return of(
            e -> getter.apply(e).floatValue() / maxGetter.apply(e).floatValue(),
            (e, value) -> setter.accept(e, converter.apply(value.floatValue() * maxGetter.apply(e).floatValue()))
        );
    }

    /**
     * Creates a swap from a getter and setter.
     */
    static <E, T> Swap<E> of(Function<E, T> getter, BiConsumer<E, T> setter) {
        return (a, b) -> {
            T aa = getter.apply(a);
            T bb = getter.apply(b);
            setter.accept(a, bb);
            setter.accept(b, aa);
        };
    }
}
