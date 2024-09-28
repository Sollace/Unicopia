package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.sollace.fabwork.api.packets.HandledPacket;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Received by the server when a player changes their opened spellbook's state
 * Received by the client when another player changes the shared spellbook's state
 */
public record MsgSpellbookStateChanged<T extends PlayerEntity> (
        int syncId,
        SpellbookState state
    ) implements HandledPacket<T> {

    public static <T extends PlayerEntity> MsgSpellbookStateChanged<T> create(SpellbookScreenHandler handler, SpellbookState state) {
        return new MsgSpellbookStateChanged<>(handler.syncId, state);
    }

    public MsgSpellbookStateChanged(RegistryByteBuf buffer) {
        this(buffer.readInt(), SpellbookState.PACKET_CODEC.decode(buffer));
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(syncId);
        SpellbookState.PACKET_CODEC.encode((RegistryByteBuf)buffer, state);
    }

    @Override
    public void handle(T sender) {
        if (sender.currentScreenHandler.syncId != syncId) {
            return;
        }

        if (sender.currentScreenHandler instanceof SpellbookScreenHandler spellbook) {
            spellbook.getSpellbookState().copyFrom(state);
            if (sender instanceof ServerPlayerEntity) {
                spellbook.getSpellbookState().synchronize();
            }
        }
    }
}
