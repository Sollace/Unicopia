package com.minelittlepony.unicopia.entity.duck;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.player.Pony;

public interface PlayerEntityDuck extends Equine.Container<Pony> {
    void callUpdateCapeAngles();
}
