package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.util.network.S2CBroadcastPacketType;
import com.minelittlepony.unicopia.util.network.S2CPacketType;
import com.minelittlepony.unicopia.util.network.C2SPacketType;
import com.minelittlepony.unicopia.util.network.SimpleNetworking;

import net.minecraft.util.Identifier;

public interface Channel {
    C2SPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = SimpleNetworking.clientToServer(new Identifier("unicopia", "player_ability"), MsgPlayerAbility::new);
    C2SPacketType<MsgRequestCapabilities> CLIENT_REQUEST_CAPABILITIES = SimpleNetworking.clientToServer(new Identifier("unicopia", "request_capabilities"), MsgRequestCapabilities::new);

    S2CPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClient(new Identifier("unicopia", "player_capabilities"), MsgPlayerCapabilities::new);
    S2CPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = SimpleNetworking.serverToClient(new Identifier("unicopia", "projectile_entity"), MsgSpawnProjectile::new);
    S2CPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = SimpleNetworking.serverToClient(new Identifier("unicopia", "block_destruction"), MsgBlockDestruction::new);

    S2CBroadcastPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClients(new Identifier("unicopia", "other_player_capabilities"), MsgOtherPlayerCapabilities::new);

    static void bootstrap() { }
}
