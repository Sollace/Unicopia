package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.util.network.S2CBroadcastPacketType;
import com.minelittlepony.unicopia.util.network.S2CPacketType;
import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.C2SPacketType;
import com.minelittlepony.unicopia.util.network.SimpleNetworking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface Channel {
    C2SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = SimpleNetworking.clientToServer(Unicopia.id("player_ability"), MsgPlayerAbility::new);
    C2SPacketType<MsgRequestSpeciesChange> CLIENT_REQUEST_SPECIES_CHANGE = SimpleNetworking.clientToServer(Unicopia.id("request_capabilities"), MsgRequestSpeciesChange::new);
    C2SPacketType<MsgMarkTraitRead> MARK_TRAIT_READ = SimpleNetworking.clientToServer(Unicopia.id("mark_trait_read"), MsgMarkTraitRead::new);
    C2SPacketType<MsgRemoveSpell> REMOVE_SPELL = SimpleNetworking.clientToServer(Unicopia.id("remove_spell"), MsgRemoveSpell::new);

    S2CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClient(Unicopia.id("player_capabilities"), MsgPlayerCapabilities::new);
    S2CPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = SimpleNetworking.serverToClient(Unicopia.id("projectile_entity"), MsgSpawnProjectile::new);
    S2CPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = SimpleNetworking.serverToClient(Unicopia.id("block_destruction"), MsgBlockDestruction::new);
    S2CPacketType<MsgCancelPlayerAbility> CANCEL_PLAYER_ABILITY = SimpleNetworking.serverToClient(Unicopia.id("player_ability_cancel"), MsgCancelPlayerAbility::new);
    S2CPacketType<MsgUnlockTraits> UNLOCK_TRAITS = SimpleNetworking.serverToClient(Unicopia.id("unlock_traits"), MsgUnlockTraits::new);

    Identifier SERVER_SELECT_TRIBE_ID = Unicopia.id("select_tribe");
    S2CPacketType<MsgTribeSelect> SERVER_SELECT_TRIBE = SimpleNetworking.serverToClient(SERVER_SELECT_TRIBE_ID, MsgTribeSelect::new);

    S2CPacketType<MsgSpellbookStateChanged<PlayerEntity>> SERVER_SPELLBOOK_UPDATE = SimpleNetworking.serverToClient(Unicopia.id("server_spellbook_update"), MsgSpellbookStateChanged::new);
    C2SPacketType<MsgSpellbookStateChanged<ServerPlayerEntity>> CLIENT_SPELLBOOK_UPDATE = SimpleNetworking.clientToServer(Unicopia.id("client_spellbook_update"), MsgSpellbookStateChanged::new);

    Identifier SERVER_RESOURCES_SEND_ID = Unicopia.id("resources_send");
    S2CPacketType<MsgServerResources> SERVER_RESOURCES_SEND = SimpleNetworking.serverToClient(SERVER_RESOURCES_SEND_ID, MsgServerResources::new);

    S2CBroadcastPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClients(Unicopia.id("other_player_capabilities"), MsgOtherPlayerCapabilities::new);
    S2CBroadcastPacketType<MsgPlayerAnimationChange> SERVER_PLAYER_ANIMATION_CHANGE = SimpleNetworking.serverToClients(Unicopia.id("other_player_animation_change"), MsgPlayerAnimationChange::new);

    static void bootstrap() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Pony pony = Pony.of(handler.player);
            if (!pony.isSpeciesPersisted()) {
                Race race = WorldTribeManager.forWorld(handler.player.getWorld()).getDefaultRace();
                if (!race.isPermitted(handler.player)) {
                    race = Race.HUMAN;
                }
                if (race.isUsable()) {
                    pony.setSpecies(race);
                    Unicopia.LOGGER.info("Setting {}'s race to {} due to host setting", handler.player.getDisplayName().getString(), Race.REGISTRY.getId(race).toString());
                } else {
                    sender.sendPacket(SERVER_SELECT_TRIBE.getId(), new MsgTribeSelect(handler.player).toBuffer());
                }
            }
            sender.sendPacket(SERVER_RESOURCES_SEND.getId(), new MsgServerResources().toBuffer());
        });
    }
}
