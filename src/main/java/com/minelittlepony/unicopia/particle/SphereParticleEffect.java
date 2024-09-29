package com.minelittlepony.unicopia.particle;

import org.joml.Vector3f;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.util.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;

public record SphereParticleEffect (
        ParticleType<? extends SphereParticleEffect> type,
        Vector3f color,
        float alpha,
        float radius,
        Vec3d offset
    ) implements ParticleEffect {
    private static final Vec3d DEFAULT_OFFSET = new Vec3d(0, 0.5, 0);
    public static MapCodec<SphereParticleEffect> createCodec(ParticleType<SphereParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.VECTOR_3F.fieldOf("color").forGetter(SphereParticleEffect::color),
            Codec.FLOAT.fieldOf("alpha").forGetter(SphereParticleEffect::alpha),
            Codec.FLOAT.fieldOf("radius").forGetter(SphereParticleEffect::radius),
            CodecUtils.VECTOR.fieldOf("offset").forGetter(SphereParticleEffect::offset)
        ).apply(instance, (color, alpha, radius, offset) -> new SphereParticleEffect(type, color, alpha, radius, offset)));
    }

    public static final PacketCodec<RegistryByteBuf, SphereParticleEffect> createPacketCodec(ParticleType<SphereParticleEffect> type) {
        return PacketCodec.tuple(
                PacketCodecs.VECTOR3F, SphereParticleEffect::color,
                PacketCodecs.FLOAT, SphereParticleEffect::alpha,
                PacketCodecs.FLOAT, SphereParticleEffect::radius,
                PacketCodecUtils.VECTOR, SphereParticleEffect::offset,
                (color, alpha, radius, offset) -> new SphereParticleEffect(type, color, alpha, radius, offset)
        );
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, int tint, float alpha, float rad) {
        this(type, tint, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, Vector3f color, float alpha, float rad) {
        this(type, color, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, int tint, float alpha, float rad, Vec3d offset) {
        this(type, new Vector3f(Color.r(tint) * 255, Color.g(tint) * 255, Color.b(tint) * 255), alpha, rad, offset);
    }

    public SphereParticleEffect withOffset(Vec3d offset) {
        return new SphereParticleEffect(type, color, alpha, radius, offset);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
