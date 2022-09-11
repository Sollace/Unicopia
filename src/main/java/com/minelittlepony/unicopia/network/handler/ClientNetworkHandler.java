package com.minelittlepony.unicopia.network.handler;

import com.minelittlepony.unicopia.network.*;

public interface ClientNetworkHandler {

    void handleTribeScreen(MsgTribeSelect packet);

    void handleSpawnProjectile(MsgSpawnProjectile packet);

    void handleBlockDestruction(MsgBlockDestruction packet);

    void handleCancelAbility(MsgCancelPlayerAbility packet);

    void handleUnlockTraits(MsgUnlockTraits packet);

    void handleServerResources(MsgServerResources packet);
}
