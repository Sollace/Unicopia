package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.data.Rot;

public interface OrientedSpell extends Spell {
    void setOrientation(float pitch, float yaw);

    default void setOrientation(Rot rotation) {
        setOrientation(rotation.pitch(), rotation.yaw());
    }
}
