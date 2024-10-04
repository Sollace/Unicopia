package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.network.track.MsgTrackedValues;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.sollace.fabwork.api.packets.*;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface Channel {
    C2SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = SimpleNetworking.clientToServer(Unicopia.id("player_ability"), MsgPlayerAbility.PACKET_CODEC);
    C2SPacketType<MsgCasterLookRequest.Reply> CLIENT_CASTER_LOOK = SimpleNetworking.clientToServer(Unicopia.id("caster_look"), MsgCasterLookRequest.Reply.PACKET_CODEC);
    C2SPacketType<MsgRequestSpeciesChange> CLIENT_REQUEST_SPECIES_CHANGE = SimpleNetworking.clientToServer(Unicopia.id("request_capabilities"), MsgRequestSpeciesChange.PACKET_CODEC);
    C2SPacketType<MsgMarkTraitRead> MARK_TRAIT_READ = SimpleNetworking.clientToServer(Unicopia.id("mark_trait_read"), MsgMarkTraitRead.PACKET_CODEC);
    C2SPacketType<MsgRemoveSpell> REMOVE_SPELL = SimpleNetworking.clientToServer(Unicopia.id("remove_spell"), MsgRemoveSpell.PACKET_CODEC);
    C2SPacketType<MsgPlayerFlightControlsInput> FLIGHT_CONTROLS_INPUT = SimpleNetworking.clientToServer(Unicopia.id("flight_controls"), MsgPlayerFlightControlsInput.PACKET_CODEC);

    S2CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClient(Unicopia.id("player_capabilities"), MsgPlayerCapabilities.PACKET_CODEC);
    S2CPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = SimpleNetworking.serverToClient(Unicopia.id("block_destruction"), MsgBlockDestruction.PACKET_CODEC);
    S2CPacketType<MsgCancelPlayerAbility> CANCEL_PLAYER_ABILITY = SimpleNetworking.serverToClient(Unicopia.id("player_ability_cancel"), MsgCancelPlayerAbility.PACKET_CODEC);
    S2CPacketType<MsgCasterLookRequest> SERVER_REQUEST_PLAYER_LOOK = SimpleNetworking.serverToClient(Unicopia.id("request_player_look"), MsgCasterLookRequest.PACKET_CODEC);
    S2CPacketType<MsgUnlockTraits> UNLOCK_TRAITS = SimpleNetworking.serverToClient(Unicopia.id("unlock_traits"), MsgUnlockTraits.PACKET_CODEC);

    S2CPacketType<MsgTribeSelect> SERVER_SELECT_TRIBE = SimpleNetworking.serverToClient(Unicopia.id("select_tribe"), MsgTribeSelect.PACKET_CODEC);

    S2CPacketType<MsgSpellbookStateChanged<PlayerEntity>> SERVER_SPELLBOOK_UPDATE = SimpleNetworking.serverToClient(Unicopia.id("server_spellbook_update"), MsgSpellbookStateChanged.packetCodec());
    C2SPacketType<MsgSpellbookStateChanged<ServerPlayerEntity>> CLIENT_SPELLBOOK_UPDATE = SimpleNetworking.clientToServer(Unicopia.id("client_spellbook_update"), MsgSpellbookStateChanged.packetCodec());

    S2CPacketType<MsgServerResources> SERVER_RESOURCES = SimpleNetworking.serverToClient(Unicopia.id("resources"), MsgServerResources.PACKET_CODEC);

    S2CPacketType<MsgTrackedValues> SERVER_TRACKED_ENTITY_DATA = SimpleNetworking.serverToClient(Unicopia.id("tracked_entity_data"), MsgTrackedValues.PACKET_CODEC);
    S2CPacketType<MsgPlayerAnimationChange> SERVER_PLAYER_ANIMATION_CHANGE = SimpleNetworking.serverToClient(Unicopia.id("player_animation_change"), MsgPlayerAnimationChange.PACKET_CODEC);
    S2CPacketType<MsgSkyAngle> SERVER_SKY_ANGLE = SimpleNetworking.serverToClient(Unicopia.id("sky_angle"), MsgSkyAngle.PACKET_CODEC);
    S2CPacketType<MsgConfigurationChange> CONFIGURATION_CHANGE = SimpleNetworking.serverToClient(Unicopia.id("config"), MsgConfigurationChange.PACKET_CODEC);
    S2CPacketType<MsgZapAppleStage> SERVER_ZAP_STAGE = SimpleNetworking.serverToClient(Unicopia.id("zap_stage"), MsgZapAppleStage.PACKET_CODEC);
    S2CPacketType<MsgTrinketBroken> SERVER_TRINKET_BROKEN = SimpleNetworking.serverToClient(Unicopia.id("trinket_broken"), MsgTrinketBroken.PACKET_CODEC);

    static void bootstrap() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Pony pony = Pony.of(handler.player);
            if (pony.getSpecies() == Race.UNSET) {
                Race race = UnicopiaWorldProperties.forWorld(handler.player.getServerWorld()).getDefaultRace();
                if (!race.isPermitted(handler.player)) {
                    race = Race.UNSET;
                }
                if (race.isUnset()) {
                    sender.sendPacket(SERVER_SELECT_TRIBE.toPacket(new MsgTribeSelect(Race.allPermitted(handler.player), "gui.unicopia.tribe_selection.welcome")));
                } else {
                    pony.setSpecies(race);
                    Unicopia.LOGGER.info("Setting {}'s race to {} due to host setting", handler.player.getDisplayName().getString(), Race.REGISTRY.getId(race).toString());
                }
            }
            sender.sendPacket(SERVER_RESOURCES.toPacket(new MsgServerResources()));
            sender.sendPacket(SERVER_SKY_ANGLE.toPacket(new MsgSkyAngle(UnicopiaWorldProperties.forWorld(handler.getPlayer().getServerWorld()).getTangentalSkyAngle())));
            sender.sendPacket(CONFIGURATION_CHANGE.toPacket(new MsgConfigurationChange(InteractionManager.getInstance().getSyncedConfig())));
            sender.sendPacket(SERVER_ZAP_STAGE.toPacket(new MsgZapAppleStage(ZapAppleStageStore.get(handler.player.getServerWorld()).getStage())));
        });
    }
}
