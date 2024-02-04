package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public record MsgZapAppleStage (
        ZapAppleStageStore.Stage stage
    ) implements Packet<PlayerEntity> {

    public MsgZapAppleStage(PacketByteBuf buffer) {
        this(buffer.readEnumConstant(ZapAppleStageStore.Stage.class));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeEnumConstant(stage);
    }
}
