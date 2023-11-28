package com.minelittlepony.unicopia.diet;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;

public record Range(int min, int max) {
    public static final Codec<Range> CODEC = Codecs.xor(
            Codec.INT.xmap(value -> Range.of(value, -1), range -> range.min()),
            RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(Range::min),
                Codec.INT.fieldOf("max").forGetter(Range::max)
            ).apply(instance, Range::of))
    ).xmap(either -> either.left().or(either::right).get(), l -> Either.right(l));

    public static Range of(int min, int max) {
        return new Range(min, max);
    }

    public static Range of(PacketByteBuf buffer) {
        return of(buffer.readInt(), buffer.readInt());
    }

    public void toBuffer(PacketByteBuf buffer) {
        buffer.writeInt(min);
        buffer.writeInt(max);
    }

    public int getTicks(int currentTicks) {
        return clamp((min * 20) + currentTicks);
    }

    public int clamp(int value) {
        return max > 0 ? Math.min(value, max * 20) : value;
    }
}