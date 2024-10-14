package com.minelittlepony.unicopia.particle;

import org.joml.Vector3f;

import com.minelittlepony.common.util.Color;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;

public record MagicParticleEffect (
        boolean tinted,
        Vector3f color
    ) implements ParticleEffect {
    public static final MagicParticleEffect UNICORN = new MagicParticleEffect(false, new Vector3f());
    public static final MapCodec<MagicParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("tinted").forGetter(MagicParticleEffect::tinted),
            Codecs.VECTOR_3F.fieldOf("color").forGetter(MagicParticleEffect::color)
    ).apply(instance, MagicParticleEffect::new));
    public static final PacketCodec<ByteBuf, MagicParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, MagicParticleEffect::tinted,
            PacketCodecs.VECTOR_3F, MagicParticleEffect::color,
            MagicParticleEffect::new
    );

    public MagicParticleEffect(int tint) {
        this(true, new Vector3f(Color.r(tint), Color.g(tint), Color.b(tint)));
    }

    public MagicParticleEffect(Vector3f color) {
        this(true, color);
    }

    public Vector3f getColor(Random random) {
        if (tinted()) {
            return color;
        }

        float r = random.nextBoolean() ? 0.9F : 1;
        float g = 0.3F;
        float b = random.nextBoolean() ? 0.4F : 1;

        if (random.nextBoolean()) {
            g *= 2F;
        } else if (random.nextBoolean()) {
            r *= 3.9F;
        }

        return new Vector3f(r, g, b);
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.UNICORN_MAGIC;
    }
}
