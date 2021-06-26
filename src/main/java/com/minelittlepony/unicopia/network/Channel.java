package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.util.network.S2CBroadcastPacketType;
import com.minelittlepony.unicopia.util.network.C2SPacketType;
import com.minelittlepony.unicopia.util.network.S2CPacketType;
import com.minelittlepony.unicopia.util.network.SimpleNetworking;

import net.minecraft.util.Identifier;

public interface Channel {
    S2CPacketType<MsgPlayerAbility<?>> CLIENT_PLAYER_ABILITY = SimpleNetworking.clientToServer(new Identifier("unicopia", "player_ability"), MsgPlayerAbility::new);
    S2CPacketType<MsgRequestCapabilities> CLIENT_REQUEST_CAPABILITIES = SimpleNetworking.clientToServer(new Identifier("unicopia", "request_capabilities"), MsgRequestCapabilities::new);
    S2CBroadcastPacketType<MsgOtherPlayerCapabilities> SERVER_OTHER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClients(new Identifier("unicopia", "other_player_capabilities"), MsgOtherPlayerCapabilities::new);

    C2SPacketType<MsgPlayerCapabilities> SERVER_PLAYER_CAPABILITIES = SimpleNetworking.serverToClient(new Identifier("unicopia", "player_capabilities"), MsgPlayerCapabilities::new);
    C2SPacketType<MsgSpawnProjectile> SERVER_SPAWN_PROJECTILE = SimpleNetworking.serverToClient(new Identifier("unicopia", "projectile_entity"), MsgSpawnProjectile::new);
    C2SPacketType<MsgBlockDestruction> SERVER_BLOCK_DESTRUCTION = SimpleNetworking.serverToClient(new Identifier("unicopia", "block_destruction"), MsgBlockDestruction::new);

    static void bootstrap() { }
}
