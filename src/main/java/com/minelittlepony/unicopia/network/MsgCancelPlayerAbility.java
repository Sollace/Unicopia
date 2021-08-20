package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.util.network.Packet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

/**
 * Sent to the client when an ability fails its server-side activation checks.
 */
public class MsgCancelPlayerAbility implements Packet<PlayerEntity> {

    MsgCancelPlayerAbility(PacketByteBuf buffer) {
    }

    public MsgCancelPlayerAbility() {
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
    }

    @Override
    public void handle(PlayerEntity sender) {
        InteractionManager.instance().getClientNetworkHandler().handleCancelAbility(this);
    }
}
