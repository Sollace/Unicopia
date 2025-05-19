package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.sollace.fabwork.api.packets.Handled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Received by the server when a player changes their opened spellbook's state
 * Received by the client when another player changes the shared spellbook's state
 */
public record MsgSpellbookStateChanged<T extends PlayerEntity> (
        int syncId,
        SpellbookState state
    ) implements Handled<T> {
    private static final PacketCodec<RegistryByteBuf, MsgSpellbookStateChanged<?>> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, MsgSpellbookStateChanged::syncId,
            SpellbookState.PACKET_CODEC, MsgSpellbookStateChanged::state,
            MsgSpellbookStateChanged::new
    );

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends PlayerEntity> PacketCodec<RegistryByteBuf, MsgSpellbookStateChanged<T>> packetCodec() {
        return (PacketCodec)PACKET_CODEC;
    }

    public static <T extends PlayerEntity> MsgSpellbookStateChanged<T> create(SpellbookScreenHandler handler, SpellbookState state) {
        return new MsgSpellbookStateChanged<>(handler.syncId, state);
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
