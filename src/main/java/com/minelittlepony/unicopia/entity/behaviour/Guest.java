package com.minelittlepony.unicopia.entity.behaviour;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.Entity;

public interface Guest {
    Guest NULL = new Guest() {
        @Override
        public void setHost(@Nullable Caster<?> host) { }

        @Nullable
        @Override
        public Caster<?> getHost() {
            return null;
        }
    };

    void setHost(@Nullable Caster<?> host);

    @Nullable
    Caster<?> getHost();

    static Guest of(@Nullable Entity entity) {
        return entity == null ? NULL : (Guest)entity;
    }

    default boolean hasHost() {
        return getHost() != null;
    }

    default boolean hostIs(Caster<?> self) {
        return getHost() == self;
    }
}
