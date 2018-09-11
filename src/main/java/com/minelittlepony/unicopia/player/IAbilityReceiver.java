package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.power.IPower;

public interface IAbilityReceiver {

    void tryUseAbility(IPower<?> power);

    void tryClearAbility();

    int getRemainingCooldown();
}
