package com.minelittlepony.unicopia.network.handler;

import java.util.Map;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.data.Rot;
import com.minelittlepony.unicopia.ability.data.tree.TreeTypes;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import com.minelittlepony.unicopia.client.DiscoveryToast;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.client.gui.TribeSelectionScreen;
import com.minelittlepony.unicopia.client.gui.spellbook.ClientChapters;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookChapterList.Chapter;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.*;
import com.minelittlepony.unicopia.network.MsgCasterLookRequest.Reply;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class ClientNetworkHandlerImpl {
    private final MinecraftClient client = MinecraftClient.getInstance();

    public ClientNetworkHandlerImpl() {
        Channel.SERVER_SELECT_TRIBE.receiver().addPersistentListener(this::handleTribeScreen);
        Channel.SERVER_SPAWN_PROJECTILE.receiver().addPersistentListener(this::handleSpawnProjectile);
        Channel.SERVER_BLOCK_DESTRUCTION.receiver().addPersistentListener(this::handleBlockDestruction);
        Channel.CANCEL_PLAYER_ABILITY.receiver().addPersistentListener(this::handleCancelAbility);
        Channel.UNLOCK_TRAITS.receiver().addPersistentListener(this::handleUnlockTraits);
        Channel.SERVER_RESOURCES_SEND.receiver().addPersistentListener(this::handleServerResources);
        Channel.SERVER_SKY_ANGLE.receiver().addPersistentListener(this::handleSkyAngle);
        Channel.SERVER_PLAYER_ANIMATION_CHANGE.receiver().addPersistentListener(this::handlePlayerAnimation);
        Channel.SERVER_REQUEST_PLAYER_LOOK.receiver().addPersistentListener(this::handleCasterLookRequest);
    }

    private void handleTribeScreen(PlayerEntity sender, MsgTribeSelect packet) {
        client.setScreen(new TribeSelectionScreen(packet.availableRaces(), packet.serverMessage()));
    }

    @SuppressWarnings("unchecked")
    private void handleSpawnProjectile(PlayerEntity sender, MsgSpawnProjectile packet) {
        ClientWorld world = client.world;
        Entity entity = packet.getEntityType().create(world);

        entity.updateTrackedPosition(packet.getX(), packet.getY(), packet.getZ());
        entity.refreshPositionAfterTeleport(packet.getX(), packet.getY(), packet.getZ());
        entity.setVelocity(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
        entity.setPitch(packet.getPitch() * 360 / 256F);
        entity.setYaw(packet.getYaw() * 360 / 256F);
        entity.setId(packet.getId());
        entity.setUuid(packet.getUuid());

        if (entity instanceof Owned.Mutable) {
            ((Owned.Mutable<Entity>) entity).setMaster(world.getEntityById(packet.getEntityData()));
        }

        if (entity.getType() == UEntities.MAGIC_BEAM) {
            InteractionManager.instance().playLoopingSound(entity, InteractionManager.SOUND_MAGIC_BEAM, entity.getId());
        }

        world.addEntity(packet.getId(), entity);
    }

    private void handleBlockDestruction(PlayerEntity sender, MsgBlockDestruction packet) {
        ClientBlockDestructionManager destr = ((ClientBlockDestructionManager.Source)client.worldRenderer).getDestructionManager();

        packet.destructions().forEach((i, d) -> {
            destr.setBlockDestruction(i, d);
        });
    }

    private void handleCancelAbility(PlayerEntity sender, MsgCancelPlayerAbility packet) {
        client.player.playSound(USounds.GUI_ABILITY_FAIL, 1, 1);
        Pony.of(client.player).getAbilities().getStats().forEach(s -> s.setCooldown(0));
    }

    private void handleUnlockTraits(PlayerEntity sender, MsgUnlockTraits packet) {
        for (Trait trait : packet.traits()) {
            DiscoveryToast.show(client.getToastManager(), trait.getSprite());
        }
    }

    private void handleSkyAngle(PlayerEntity sender, MsgSkyAngle packet) {
        UnicopiaClient.getInstance().tangentalSkyAngle.update(packet.tangentalSkyAngle(), 200);
    }

    @SuppressWarnings("unchecked")
    private void handleServerResources(PlayerEntity sender, MsgServerResources packet) {
        SpellTraits.load(packet.traits());
        ClientChapters.load((Map<Identifier, Chapter>)packet.chapters());
        TreeTypes.load(packet.treeTypes());
    }

    private void handlePlayerAnimation(PlayerEntity sender, MsgPlayerAnimationChange packet) {
        Pony player = Pony.of(MinecraftClient.getInstance().world.getPlayerByUuid(packet.playerId()));
        if (player == null) {
            return;
        }

        player.setAnimation(packet.animation(), packet.duration());
    }

    private void handleCasterLookRequest(PlayerEntity sender, MsgCasterLookRequest packet) {
        Pony player = Pony.of(MinecraftClient.getInstance().player);
        if (player == null) {
            return;
        }

        Channel.CLIENT_CASTER_LOOK.sendToServer(new Reply(packet.spellId(), Rot.of(player)));
    }
}
