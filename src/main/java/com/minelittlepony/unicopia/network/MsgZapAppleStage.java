package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record MsgZapAppleStage (
        ZapAppleStageStore.Stage stage
    ) {
    public static final PacketCodec<ByteBuf, MsgZapAppleStage> PACKET_CODEC = ZapAppleStageStore.Stage.PACKET_CODEC.xmap(MsgZapAppleStage::new, MsgZapAppleStage::stage);
}
