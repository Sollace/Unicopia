package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.MinecraftClient;

public class MsgBlockDestruction implements Channel.Packet {

    private final BlockPos pos;

    private final int amount;

    MsgBlockDestruction(PacketByteBuf buffer) {
        pos = buffer.readBlockPos();
        amount = buffer.readByte();
    }

    public MsgBlockDestruction(BlockPos pos, int amount) {
        this.pos = pos;
        this.amount = amount;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeByte(amount);
    }

    @Override
    public void handle(PacketContext context) {
        ((ClientBlockDestructionManager.Source)MinecraftClient.getInstance().worldRenderer).getDestructionManager().setBlockDestruction(pos, amount);
    }
}
