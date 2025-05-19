package com.minelittlepony.unicopia.network;

import java.util.UUID;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.sollace.fabwork.api.packets.Handled;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;

/**
 * Sent to the server when a player dismisses a spell from their dismiss spell screen
 */
public record MsgRemoveSpell (UUID id) implements Handled<ServerPlayerEntity> {
    public static final PacketCodec<ByteBuf, MsgRemoveSpell> PACKET_CODEC = Uuids.PACKET_CODEC.xmap(MsgRemoveSpell::new, MsgRemoveSpell::id);

    public MsgRemoveSpell(Spell spell) {
        this(spell.getUuid());
    }

    @Override
    public void handle(ServerPlayerEntity sender) {
        Pony player = Pony.of(sender);
        if (player != null) {
            player.getSpellSlot().remove(id);
        }
    }
}
