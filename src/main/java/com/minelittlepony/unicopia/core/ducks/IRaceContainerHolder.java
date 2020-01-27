package com.minelittlepony.unicopia.core.ducks;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.entity.IEntity;
import com.minelittlepony.unicopia.core.magic.ICaster;

import net.minecraft.entity.LivingEntity;

public interface IRaceContainerHolder<T extends IEntity> {
    T getRaceContainer();

    @SuppressWarnings("unchecked")
    @Nullable
    default <E extends LivingEntity> ICaster<E> getCaster() {
        T ientity = getRaceContainer();

        if (ientity instanceof ICaster) {
            return (ICaster<E>)ientity;
        }
        return null;
    }

    IEntity createRaceContainer();
}
