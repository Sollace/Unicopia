package com.minelittlepony.unicopia.network;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.SpellbookState;
import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Received by the server when a player changes their opened spellbook's state
 * Received by the client when another player changes the shared spellbook's state
 */
public class MsgSpellbookStateChanged<T extends PlayerEntity> implements Packet<T> {

    private final int syncId;
    private final SpellbookState state;

    public MsgSpellbookStateChanged(int syncId, SpellbookState state) {
        this.syncId = syncId;
        this.state = state;
    }

    public MsgSpellbookStateChanged(PacketByteBuf buffer) {
        syncId = buffer.readInt();
        state = new SpellbookState().fromPacket(buffer);
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(syncId);
        state.toPacket(buffer);
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
