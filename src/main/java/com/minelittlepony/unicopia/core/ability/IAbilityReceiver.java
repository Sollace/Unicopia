package com.minelittlepony.unicopia.core.ability;

import com.minelittlepony.unicopia.core.ability.IPower;

public interface IAbilityReceiver {

    void tryUseAbility(IPower<?> power);

    void tryClearAbility();

    int getRemainingCooldown();
}
