package com.minelittlepony.unicopia.diet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record FoodAttributes(FoodComponent food) {
    public static final float SNACK_SECONDS = 0.8F;
    public static final float NORMAL_SECONDS = 1.6F;
    public static final Codec<FoodAttributes> CODEC = RecordCodecBuilder.create(i -> i.group(
            FoodComponent.CODEC.fieldOf("food").forGetter(FoodAttributes::food)
    ).apply(i, FoodAttributes::new));
    public static final PacketCodec<RegistryByteBuf, FoodAttributes> PACKET_CODEC = PacketCodec.tuple(
            FoodComponent.PACKET_CODEC, FoodAttributes::food,
            FoodAttributes::new
    );
}
