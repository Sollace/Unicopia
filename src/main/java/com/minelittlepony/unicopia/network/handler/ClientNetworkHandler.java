package com.minelittlepony.unicopia.network.handler;

import java.util.Map;

import com.minelittlepony.unicopia.network.*;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface ClientNetworkHandler {

    void handleTribeScreen(MsgTribeSelect packet);

    void handleSpawnProjectile(MsgSpawnProjectile packet);

    void handleBlockDestruction(MsgBlockDestruction packet);

    void handleCancelAbility(MsgCancelPlayerAbility packet);

    void handleUnlockTraits(MsgUnlockTraits packet);

    void handleServerResources(MsgServerResources packet);

    Map<Identifier, ?> readChapters(PacketByteBuf buf);
}
