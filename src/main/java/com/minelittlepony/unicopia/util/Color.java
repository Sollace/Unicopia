package com.minelittlepony.unicopia.util;

public interface Color {
    static float r(int hex) {
        return (hex >> 16 & 255) / 255F;
    }

    static float g(int hex) {
        return (hex >> 8 & 255) / 255F;
    }

    static float b(int hex) {
        return (hex & 255) / 255F;
    }
}
