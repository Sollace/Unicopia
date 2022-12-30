package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sent to the server when a player activates an ability.
 */
public record MsgRemoveSpell (UUID id) implements Packet<ServerPlayerEntity> {
    MsgRemoveSpell(PacketByteBuf buffer) {
        this(buffer.readUuid());
    }

    public MsgRemoveSpell(Spell spell) {
        this(spell.getUuid());
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
