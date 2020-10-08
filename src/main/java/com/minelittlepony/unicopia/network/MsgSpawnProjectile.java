package com.minelittlepony.unicopia.network;

import java.io.IOException;
import java.util.Optional;

import com.minelittlepony.unicopia.Owned;

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

    @SuppressWarnings("unchecked")
    public MsgSpawnProjectile(Entity e) {
        super(e, Optional.of(e instanceof Owned ? ((Owned<Entity>)e).getMaster() : null).map(Entity::getEntityId).orElse(0));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        try {
            write(buffer);
        } catch (IOException e) {
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(PacketContext context) {
        World world = context.getPlayer().world;
        Entity entity = getEntityTypeId().create(world);

        entity.updateTrackedPosition(getX(), getY(), getZ());
        entity.refreshPositionAfterTeleport(getX(), getY(), getZ());
        entity.setVelocity(getVelocityX(), getVelocityY(), getVelocityZ());
        entity.pitch = getPitch() * 360 / 256F;
        entity.yaw = getYaw() * 360 / 256F;
        entity.setEntityId(getId());
        entity.setUuid(getUuid());

        if (entity instanceof Owned) {
            ((Owned<Entity>) entity).setMaster(world.getEntityById(this.getEntityData()));
        }

        ((ClientWorld)world).addEntity(getId(), entity);
    }
}






