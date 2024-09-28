package com.minelittlepony.unicopia.diet.affliction;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Range(int min, int max) {
    private static final Interner<Range> INTERNER = Interners.newWeakInterner();
    public static final Codec<Range> CODEC = Codec.xor(
            Codec.INT.xmap(value -> Range.of(value, -1), range -> range.min()),
            RecordCodecBuilder.<Range>create(instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(Range::min),
                Codec.INT.fieldOf("max").forGetter(Range::max)
            ).apply(instance, Range::of))
    ).xmap(either -> either.left().or(either::right).get(), l -> Either.right(l));
    public static final PacketCodec<PacketByteBuf, Range> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, Range::min,
            PacketCodecs.INTEGER, Range::max,
            Range::of
    );

    public static Range of(int min, int max) {
        return INTERNER.intern(new Range(min, max));
    }

    public static Range of(int exact) {
        return of(exact, exact);
    }

    public int getClamped(int currentTicks, int multiplier) {
        return clamp((min * multiplier) + currentTicks, multiplier);
    }

    public int clamp(int value, int multiplier) {
        return max > 0 ? Math.min(value, max * multiplier) : value;
    }
}