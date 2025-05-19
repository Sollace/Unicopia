package com.minelittlepony.unicopia.block.state;

import java.util.Arrays;
import java.util.function.Supplier;

class ExpandableList<T> {
    private final Supplier<T> defaultValue;
    private Object[] values;
    private int size;

    public ExpandableList(int initialCapacity, Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        values = new Object[Math.max(0, initialCapacity)];
    }

    public int size() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        T t = (T)values[index];
        if (t == null) {
            values[index] = t = defaultValue.get();
        }
        return t;
    }

    public T getOrExpand(int index) {
        resize(index);
        return get(index);
    }

    public void set(int index, T value) {
        resize(index);
        values[index] = value;
    }

    private void resize(int index) {
        if (index >= values.length) {
            values = Arrays.copyOf(values, Math.max(index + 1, (values.length + 1) * 2));
        }
        size = Math.max(index + 1, size);
    }
}
