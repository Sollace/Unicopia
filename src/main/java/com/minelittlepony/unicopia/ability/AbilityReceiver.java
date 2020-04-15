package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.ability.Ability;

public interface AbilityReceiver {

    void tryUseAbility(Ability<?> power);

    void tryClearAbility();

    int getRemainingCooldown();
}
