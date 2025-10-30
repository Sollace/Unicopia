package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record SkinFeatures(boolean showHorn, boolean showWings, Optional<Integer> skinColor) {
    public static final SkinFeatures DEFAULT = new SkinFeatures(true, true, Optional.empty());
    public static final Codec<SkinFeatures> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("show_horn").forGetter(SkinFeatures::showHorn),
            Codec.BOOL.fieldOf("show_wings").forGetter(SkinFeatures::showWings),
            Codec.INT.optionalFieldOf("skin_color").forGetter(SkinFeatures::skinColor)
    ).apply(i, SkinFeatures::new));
    public static final PacketCodec<RegistryByteBuf, SkinFeatures> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, SkinFeatures::showHorn,
            PacketCodecs.BOOL, SkinFeatures::showWings,
            PacketCodecs.optional(PacketCodecs.INTEGER), SkinFeatures::skinColor,
            SkinFeatures::new
    );
}
