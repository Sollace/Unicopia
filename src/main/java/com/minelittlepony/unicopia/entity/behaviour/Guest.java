package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;

public interface Guest {
    void setHost(Caster<?> host);

    Caster<?> getHost();
}
