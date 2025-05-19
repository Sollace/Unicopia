package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.magic.Caster;

public interface OrientedSpell extends Spell {
    void setOrientation(Caster<?> caster, float pitch, float yaw);

    default void setOrientation(Caster<?> caster, Rot rotation) {
        setOrientation(caster, rotation.pitch(), rotation.yaw());
    }
}
