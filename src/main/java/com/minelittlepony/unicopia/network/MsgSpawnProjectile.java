package com.minelittlepony.unicopia.network;

import java.io.IOException;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;

public class MsgSpawnProjectile extends EntitySpawnS2CPacket implements Channel.Packet {

    MsgSpawnProjectile(PacketByteBuf buffer) {
        try {
            read(buffer);
        } catch (IOException e) { }
    }

    public MsgSpawnProjectile(Entity e) {
        super(e);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        try {
            write(buffer);
        } catch (IOException e) {
        }
    }

    @Override
    public void handle(PacketContext context) {
        World world = context.getPlayer().world;
        Entity entity = getEntityTypeId().create(world);

        entity.updateTrackedPosition(getX(), getY(), getZ());
        entity.pitch = getPitch() * 360 / 256.0F;
        entity.yaw = getYaw() * 360 / 256.0F;
        entity.setEntityId(getId());
        entity.setUuid(getUuid());
        ((ClientWorld)world).addEntity(getId(), entity);
    }
}






