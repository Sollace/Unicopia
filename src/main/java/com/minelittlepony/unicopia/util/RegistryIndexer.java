package com.minelittlepony.unicopia.util;

import com.minelittlepony.common.client.gui.IField;

import net.minecraft.util.registry.Registry;

public class RegistryIndexer<T> {

    public static <T> RegistryIndexer<T> of(Registry<T> registry) {
        return new RegistryIndexer<>(registry);
    }

    private final Registry<T> values;

    private RegistryIndexer(Registry<T> registry) {
        values = registry;
    }

    public int size() {
        return values.size() - 1;
    }

    public int indexOf(T value) {
        return values.getRawId(value);
    }

    public T valueOf(int index) {
        return values.get(wrapIndex(index));
    }

    public T valueOf(float index) {
        return valueOf((int)index);
    }

    public T cycle(T value, int increment) {
        return valueOf(indexOf(value) + increment);
    }

    public IField.IChangeCallback<Float> createSetter(IField.IChangeCallback<T> setter) {
        return index -> {
            int i = wrapIndex(index.intValue());
            setter.perform(valueOf(i));
            return (float)i;
        };
    }

    private int wrapIndex(int index) {
        int sz = values.size();
        while (index < 0) {
            index += sz;
        }
        return index % sz;
    }
}
