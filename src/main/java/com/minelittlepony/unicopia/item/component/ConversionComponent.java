package com.minelittlepony.unicopia.item.component;

import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record ConversionComponent(RegistryKey<Item> convertsInto) {
    public static final Codec<ConversionComponent> CODEC = RegistryKey.createCodec(RegistryKeys.ITEM).xmap(ConversionComponent::new, ConversionComponent::convertsInto);
    public static final PacketCodec<RegistryByteBuf, ConversionComponent> PACKET_CODEC = PacketCodec.tuple(
            RegistryKey.createPacketCodec(RegistryKeys.ITEM), ConversionComponent::convertsInto,
            ConversionComponent::new
    );

    public Optional<Item> getItem() {
        return Registries.ITEM.getOptionalValue(convertsInto());
    }
}
