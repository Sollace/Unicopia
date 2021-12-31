package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.network.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sent to the server when a player activates an ability.
 */
public class MsgRemoveSpell implements Packet<ServerPlayerEntity> {
    private final UUID id;

    MsgRemoveSpell(PacketByteBuf buffer) {
        id = buffer.readUuid();
    }

    public MsgRemoveSpell(Spell spell) {
        id = spell.getUuid();
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeUuid(id);
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony player = Pony.of(sender);
        if (player != null) {
            player.getSpellSlot().removeIf(spell -> spell.getUuid().equals(id), true);
        }
    }
}
