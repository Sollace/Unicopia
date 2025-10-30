package com.minelittlepony.unicopia.compat.trinkets;

public record SlotKey(String group, String name) implements Comparable<SlotKey> {
    public static SlotKey of(String group, String name) {
        return new SlotKey(group, name);
    }

    @Override
    public int compareTo(SlotKey o) {
        int g = group.compareTo(o.group());
        if (g == 0) {
            g = name.compareTo(o.name());
        }
        return g;
    }

    @Override
    public String toString() {
        return group + ":" + name;
    }
}
