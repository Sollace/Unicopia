package com.minelittlepony.unicopia.diet;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record FoodAttributes(FoodComponent food, Optional<ConsumableComponent> consumable) {
    public static final float SNACK_SECONDS = 0.8F;
    public static final float NORMAL_SECONDS = 1.6F;

    public static final Codec<FoodAttributes> CODEC = RecordCodecBuilder.create(i -> i.group(
            FoodComponent.CODEC.fieldOf("food").forGetter(FoodAttributes::food),
            ConsumableComponent.CODEC.optionalFieldOf("consumable").forGetter(FoodAttributes::consumable)
    ).apply(i, FoodAttributes::new));
    public static final PacketCodec<RegistryByteBuf, FoodAttributes> PACKET_CODEC = PacketCodec.tuple(
            FoodComponent.PACKET_CODEC, FoodAttributes::food,
            PacketCodecs.optional(ConsumableComponent.PACKET_CODEC), FoodAttributes::consumable,
            FoodAttributes::new
    );

    public ConsumableComponent getConsumableComponent(@Nullable ConsumableComponent existing) {
        return consumable.orElse(MoreObjects.firstNonNull(existing, ConsumableComponents.FOOD));
    }
}
