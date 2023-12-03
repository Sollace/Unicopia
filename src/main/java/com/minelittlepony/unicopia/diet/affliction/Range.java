package com.minelittlepony.unicopia.diet.affliction;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.dynamic.Codecs;

record Range(int min, int max) {
    public static final Codec<Range> CODEC = Codecs.xor(
            Codec.INT.xmap(value -> Range.of(value, -1), range -> range.min()),
            RecordCodecBuilder.<Range>create(instance -> instance.group(
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

    public int getClamped(int currentTicks, int multiplier) {
        return clamp((min * multiplier) + currentTicks, multiplier);
    }

    public int clamp(int value, int multiplier) {
        return max > 0 ? Math.min(value, max * multiplier) : value;
    }
}