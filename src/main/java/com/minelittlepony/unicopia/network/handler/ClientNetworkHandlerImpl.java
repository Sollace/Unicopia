package com.minelittlepony.unicopia.network.handler;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import com.minelittlepony.unicopia.client.gui.TribeSelectionScreen;
import com.minelittlepony.unicopia.network.MsgBlockDestruction;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;
import com.minelittlepony.unicopia.network.MsgTribeSelect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public class ClientNetworkHandlerImpl implements ClientNetworkHandler {

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void handleTribeScreen(MsgTribeSelect packet) {
        client.openScreen(new TribeSelectionScreen(packet.getRaces()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleSpawnProjectile(MsgSpawnProjectile packet) {
        ClientWorld world = client.world;
        Entity entity = packet.getEntityTypeId().create(world);

        entity.updateTrackedPosition(packet.getX(), packet.getY(), packet.getZ());
        entity.refreshPositionAfterTeleport(packet.getX(), packet.getY(), packet.getZ());
        entity.setVelocity(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
        entity.setPitch(packet.getPitch() * 360 / 256F);
        entity.setYaw(packet.getYaw() * 360 / 256F);
        entity.setId(packet.getId());
        entity.setUuid(packet.getUuid());

        if (entity instanceof Owned) {
            ((Owned<Entity>) entity).setMaster(world.getEntityById(packet.getEntityData()));
        }

        world.addEntity(packet.getId(), entity);
    }

    @Override
    public void handleBlockDestruction(MsgBlockDestruction packet) {
        ClientBlockDestructionManager destr = ((ClientBlockDestructionManager.Source)client.worldRenderer).getDestructionManager();

        packet.getDestructions().forEach((i, d) -> {
            destr.setBlockDestruction(i, d);
        });
    }

}
