package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.util.network.S2CBroadcastPacketType;
import com.minelittlepony.unicopia.util.network.S2CPacketType;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.C2SPacketType;
import com.minelittlepony.unicopia.util.network.SimpleNetworking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;

public interface Channel {
    C2SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = SimpleNetworking.clientToServer(new Identifier("unicopia", "player_ability"), MsgPlayerAbility::new);
    C2SPacketType<MsgRequestSpeciesChange> CLIENT_REQUEST_SPECIES_CHANGE = SimpleNetworking.clientToServer(new Identifier("unicopia", "request_capabilities"), MsgRequestSpeciesChange::new);
    C2SPacketType<MsgMarkTraitRead> MARK_TRAIT_READ = SimpleNetworking.clientToServer(new Identifier("unicopia", "mark_trait_read"), MsgMarkTraitRead::new);

    S2CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClient(new Identifier("unicopia", "player_capabilities"), MsgPlayerCapabilities::new);
    S2CPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = SimpleNetworking.serverToClient(new Identifier("unicopia", "projectile_entity"), MsgSpawnProjectile::new);
    S2CPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = SimpleNetworking.serverToClient(new Identifier("unicopia", "block_destruction"), MsgBlockDestruction::new);
    S2CPacketType<MsgCancelPlayerAbility> CANCEL_PLAYER_ABILITY = SimpleNetworking.serverToClient(new Identifier("unicopia", "player_ability_cancel"), MsgCancelPlayerAbility::new);
    S2CPacketType<MsgUnlockTraits> UNLOCK_TRAITS = SimpleNetworking.serverToClient(new Identifier("unicopia", "unlock_traits"), MsgUnlockTraits::new);

    Identifier SERVER_SELECT_TRIBE_ID = new Identifier("unicopia", "select_tribe");
    S2CPacketType<MsgTribeSelect> SERVER_SELECT_TRIBE = SimpleNetworking.serverToClient(SERVER_SELECT_TRIBE_ID, MsgTribeSelect::new);

    S2CBroadcastPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClients(new Identifier("unicopia", "other_player_capabilities"), MsgOtherPlayerCapabilities::new);

    static void bootstrap() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!Pony.of(handler.player).isSpeciesPersisted()) {
                sender.sendPacket(SERVER_SELECT_TRIBE_ID, new MsgTribeSelect(handler.player).toBuffer());
            }
        });
    }
}
