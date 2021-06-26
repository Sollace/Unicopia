package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import com.minelittlepony.unicopia.util.network.Packet;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class MsgBlockDestruction implements Packet<PlayerEntity> {

    private final Long2ObjectMap<Integer> destructions;

    MsgBlockDestruction(PacketByteBuf buffer) {
        destructions = new Long2ObjectOpenHashMap<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            destructions.put(buffer.readLong(), (Integer)buffer.readInt());
        }
    }

    public MsgBlockDestruction(Long2ObjectMap<Integer> destructions) {
        this.destructions = destructions;
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(destructions.size());
        destructions.forEach((p, i) -> {
            buffer.writeLong(p);
            buffer.writeInt(i);
        });
    }

    @Override
    public void handle(PlayerEntity sender) {
        ClientBlockDestructionManager destr = ((ClientBlockDestructionManager.Source)MinecraftClient.getInstance().worldRenderer).getDestructionManager();

        destructions.forEach((i, d) -> {
            destr.setBlockDestruction(i, d);
        });

    }
}
