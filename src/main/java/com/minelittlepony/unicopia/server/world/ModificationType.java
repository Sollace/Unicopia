package com.minelittlepony.unicopia.server.world;

public enum ModificationType {
    MAGICAL,
    PHYSICAL,
    EITHER;

    public boolean checkMagical() {
        return this == MAGICAL || this == EITHER;
    }

    public boolean checkPhysical() {
        return this == PHYSICAL || this == EITHER;
    }
}
