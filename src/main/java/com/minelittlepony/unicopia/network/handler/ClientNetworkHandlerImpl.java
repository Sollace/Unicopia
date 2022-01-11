package com.minelittlepony.unicopia.network.handler;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import com.minelittlepony.unicopia.client.DiscoveryToast;
import com.minelittlepony.unicopia.client.gui.TribeSelectionScreen;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.MsgBlockDestruction;
import com.minelittlepony.unicopia.network.MsgCancelPlayerAbility;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;
import com.minelittlepony.unicopia.network.MsgTribeSelect;
import com.minelittlepony.unicopia.network.MsgUnlockTraits;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public class ClientNetworkHandlerImpl implements ClientNetworkHandler {

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Override
    public void handleTribeScreen(MsgTribeSelect packet) {
        client.setScreen(new TribeSelectionScreen(packet.getRaces()));
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

        if (entity.getType() == UEntities.MAGIC_BEAM) {
            InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_MAGIC_BEAM);
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

    @Override
    public void handleCancelAbility(MsgCancelPlayerAbility packet) {
        client.player.playSound(USounds.GUI_ABILITY_FAIL, 1, 1);
        Pony.of(client.player).getAbilities().getStats().forEach(s -> s.setCooldown(0));
    }

    @Override
    public void handleUnlockTraits(MsgUnlockTraits packet) {
        for (Trait trait : packet.traits) {
            DiscoveryToast.show(client.getToastManager(), trait.getSprite());
        }
    }
}
