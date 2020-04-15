package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.ability.IPower;

public interface IAbilityReceiver {

    void tryUseAbility(IPower<?> power);

    void tryClearAbility();

    int getRemainingCooldown();
}
