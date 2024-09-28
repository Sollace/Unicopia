package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;

public record EmptyAffliction() implements Affliction {
    public static final EmptyAffliction INSTANCE = new EmptyAffliction();
    public static final MapCodec<EmptyAffliction> CODEC = MapCodec.unit(INSTANCE);
    public static final PacketCodec<ByteBuf, EmptyAffliction> PACKET_CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public void afflict(PlayerEntity player, ItemStack stack) {
    }

    @Override
    public AfflictionType<?> getType() {
        return AfflictionType.EMPTY;
    }
}
