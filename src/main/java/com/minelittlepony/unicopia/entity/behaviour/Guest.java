package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.Entity;

public interface Guest {
    void setHost(Caster<?> host);

    Caster<?> getHost();

    static boolean hasHost(Entity entity) {
        return ((Guest)entity).getHost() != null;
    }
}
