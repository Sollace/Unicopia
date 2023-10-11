package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.sollace.fabwork.api.packets.*;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Channel {
    C2SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = SimpleNetworking.clientToServer(Unicopia.id("player_ability"), MsgPlayerAbility::read);
    C2SPacketType<MsgCasterLookRequest.Reply> CLIENT_CASTER_LOOK = SimpleNetworking.clientToServer(Unicopia.id("caster_look"), MsgCasterLookRequest.Reply::new);
    C2SPacketType<MsgRequestSpeciesChange> CLIENT_REQUEST_SPECIES_CHANGE = SimpleNetworking.clientToServer(Unicopia.id("request_capabilities"), MsgRequestSpeciesChange::new);
    C2SPacketType<MsgMarkTraitRead> MARK_TRAIT_READ = SimpleNetworking.clientToServer(Unicopia.id("mark_trait_read"), MsgMarkTraitRead::new);
    C2SPacketType<MsgRemoveSpell> REMOVE_SPELL = SimpleNetworking.clientToServer(Unicopia.id("remove_spell"), MsgRemoveSpell::new);
    C2SPacketType<MsgPlayerFlightControlsInput> FLIGHT_CONTROLS_INPUT = SimpleNetworking.clientToServer(Unicopia.id("flight_controls"), MsgPlayerFlightControlsInput::new);

    S2CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClient(Unicopia.id("player_capabilities"), MsgPlayerCapabilities::new);
    S2CPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = SimpleNetworking.serverToClient(Unicopia.id("projectile_entity"), MsgSpawnProjectile::new);
    S2CPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = SimpleNetworking.serverToClient(Unicopia.id("block_destruction"), MsgBlockDestruction::new);
    S2CPacketType<MsgCancelPlayerAbility> CANCEL_PLAYER_ABILITY = SimpleNetworking.serverToClient(Unicopia.id("player_ability_cancel"), MsgCancelPlayerAbility::read);
    S2CPacketType<MsgCasterLookRequest> SERVER_REQUEST_PLAYER_LOOK = SimpleNetworking.serverToClient(Unicopia.id("request_player_look"), MsgCasterLookRequest::new);
    S2CPacketType<MsgUnlockTraits> UNLOCK_TRAITS = SimpleNetworking.serverToClient(Unicopia.id("unlock_traits"), MsgUnlockTraits::new);

    S2CPacketType<MsgTribeSelect> SERVER_SELECT_TRIBE = SimpleNetworking.serverToClient(Unicopia.id("select_tribe"), MsgTribeSelect::new);

    S2CPacketType<MsgSpellbookStateChanged<PlayerEntity>> SERVER_SPELLBOOK_UPDATE = SimpleNetworking.serverToClient(Unicopia.id("server_spellbook_update"), MsgSpellbookStateChanged::new);
    C2SPacketType<MsgSpellbookStateChanged<ServerPlayerEntity>> CLIENT_SPELLBOOK_UPDATE = SimpleNetworking.clientToServer(Unicopia.id("client_spellbook_update"), MsgSpellbookStateChanged::new);

    S2CPacketType<MsgServerResources> SERVER_RESOURCES_SEND = SimpleNetworking.serverToClient(Unicopia.id("resources_send"), MsgServerResources::new);

    S2CPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClient(Unicopia.id("other_player_capabilities"), MsgOtherPlayerCapabilities::new);
    S2CPacketType<MsgPlayerAnimationChange> SERVER_PLAYER_ANIMATION_CHANGE = SimpleNetworking.serverToClient(Unicopia.id("other_player_animation_change"), MsgPlayerAnimationChange::new);
    S2CPacketType<MsgSkyAngle> SERVER_SKY_ANGLE = SimpleNetworking.serverToClient(Unicopia.id("sky_angle"), MsgSkyAngle::new);

    static void bootstrap() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Pony pony = Pony.of(handler.player);
            if (pony.getSpecies() == Race.UNSET) {
                Race race = UnicopiaWorldProperties.forWorld(handler.player.getServerWorld()).getDefaultRace();
                if (!race.isPermitted(handler.player)) {
                    race = Race.UNSET;
                }
                if (race.isUnset()) {
                    sender.sendPacket(SERVER_SELECT_TRIBE.id(), new MsgTribeSelect(Race.allPermitted(handler.player), "gui.unicopia.tribe_selection.welcome").toBuffer());
                } else {
                    pony.setSpecies(race);
                    Unicopia.LOGGER.info("Setting {}'s race to {} due to host setting", handler.player.getDisplayName().getString(), Race.REGISTRY.getId(race).toString());
                }
            }
            sender.sendPacket(SERVER_RESOURCES_SEND.id(), new MsgServerResources().toBuffer());
            sender.sendPacket(SERVER_SKY_ANGLE.id(), new MsgSkyAngle(UnicopiaWorldProperties.forWorld(handler.getPlayer().getServerWorld()).getTangentalSkyAngle()).toBuffer());
        });
    }
}
