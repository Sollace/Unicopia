package com.minelittlepony.unicopia.network.handler;

import com.minelittlepony.unicopia.network.MsgBlockDestruction;
import com.minelittlepony.unicopia.network.MsgCancelPlayerAbility;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;
import com.minelittlepony.unicopia.network.MsgTribeSelect;

public interface ClientNetworkHandler {

    void handleTribeScreen(MsgTribeSelect packet);

    void handleSpawnProjectile(MsgSpawnProjectile packet);

    void handleBlockDestruction(MsgBlockDestruction packet);

    void handleCancelAbility(MsgCancelPlayerAbility packet);
}
