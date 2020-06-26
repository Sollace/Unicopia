package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.world.entity.RainbowEntity;
import com.minelittlepony.unicopia.world.entity.UEntities;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

public class MsgSpawnRainbow implements Channel.Packet {

    private final int id;

    private final double x;
    private final double y;
    private final double z;

    public MsgSpawnRainbow(Entity entity) {
        id = entity.getEntityId();
        x = entity.getX();
        y = entity.getY();
        z = entity.getZ();
     }

    MsgSpawnRainbow(PacketByteBuf buffer) {
        id = buffer.readVarInt();
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeVarInt(id);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    @Override
    public void handle(PacketContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        RainbowEntity entity = UEntities.RAINBOW.create(client.world);
        entity.setPos(x, y, z);
        entity.updateTrackedPosition(x, y, z);
        entity.yaw = 0;
        entity.pitch = 0;
        entity.setEntityId(id);
        client.world.addEntity(id, entity);
    }
}
