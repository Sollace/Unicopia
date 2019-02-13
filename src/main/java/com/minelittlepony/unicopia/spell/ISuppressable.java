package com.minelittlepony.unicopia.spell;

public interface ISuppressable extends IMagicEffect {

    boolean getSuppressed();

    boolean isVulnerable(ICaster<?> otherSource, IMagicEffect other);

    void onSuppressed(ICaster<?> otherSource);
}
