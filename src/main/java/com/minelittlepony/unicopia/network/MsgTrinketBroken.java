package com.minelittlepony.unicopia.network;

import com.sollace.fabwork.api.packets.Packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

/**
 * Sent to the client when an item equipped in a trinket slot breaks
 */
public record MsgTrinketBroken (ItemStack stack, int entityId) implements Packet<PlayerEntity> {
    public MsgTrinketBroken(PacketByteBuf buffer) {
        this(buffer.readItemStack(), buffer.readInt());
    }

    @Override
    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeItemStack(stack);
        buffer.writeInt(entityId);
    }
}
