package com.minelittlepony.unicopia.particle;

import com.minelittlepony.unicopia.spell.ICaster;

public interface IAttachableParticle {

    boolean isStillAlive();

    void attachTo(ICaster<?> caster);

    void setAttribute(int key, Object value);
}
