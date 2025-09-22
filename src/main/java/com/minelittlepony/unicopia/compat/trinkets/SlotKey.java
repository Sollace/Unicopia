package com.minelittlepony.unicopia.compat.trinkets;

public record SlotKey(String group, String name) {
    public static SlotKey of(String group, String name) {
        return new SlotKey(group, name);
    }
}
