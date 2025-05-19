package com.minelittlepony.unicopia.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * Sent to the client when an item equipped in a trinket slot breaks
 */
public record MsgTrinketBroken (ItemStack stack, int entityId) {
    public static final PacketCodec<RegistryByteBuf, MsgTrinketBroken> PACKET_CODEC = PacketCodec.tuple(
            ItemStack.PACKET_CODEC, MsgTrinketBroken::stack,
            PacketCodecs.INTEGER, MsgTrinketBroken::entityId,
            MsgTrinketBroken::new
    );
}
