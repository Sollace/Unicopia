package com.minelittlepony.unicopia.network;

import java.util.Optional;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.util.network.Packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

/**
 * Sent by the server to spawn a projectile entity on the client.
 */
public class MsgSpawnProjectile extends EntitySpawnS2CPacket implements Packet<PlayerEntity> {

    MsgSpawnProjectile(PacketByteBuf buffer) {
        super(buffer);
    }

    @SuppressWarnings("unchecked")
    public MsgSpawnProjectile(Entity e) {
        super(e, Optional.ofNullable(e instanceof Owned ? ((Owned<Entity>)e).getMaster() : null).map(Entity::getId).orElse(0));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        write(buffer);
    }


    @Override
    public void handle(PlayerEntity sender) {
        if (sender.world.isClient) {
            handledByClient(sender);
        }
    }

    @Environment(EnvType.CLIENT)
    @SuppressWarnings("unchecked")
    private void handledByClient(PlayerEntity sender) {
        ClientWorld world = MinecraftClient.getInstance().world;
        Entity entity = getEntityTypeId().create(world);

        entity.updateTrackedPosition(getX(), getY(), getZ());
        entity.refreshPositionAfterTeleport(getX(), getY(), getZ());
        entity.setVelocity(getVelocityX(), getVelocityY(), getVelocityZ());
        entity.setPitch(getPitch() * 360 / 256F);
        entity.setYaw(getYaw() * 360 / 256F);
        entity.setId(getId());
        entity.setUuid(getUuid());

        if (entity instanceof Owned) {
            ((Owned<Entity>) entity).setMaster(world.getEntityById(getEntityData()));
        }

        world.addEntity(getId(), entity);
    }
}
