package com.minelittlepony.unicopia.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public record SpiralParticleEffect(
        WeakTarget centerPoint,
        float angularVelocity,
        ParticleEffect effect
    ) implements ParticleEffect {
    public static final MapCodec<SpiralParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WeakTarget.CODEC.fieldOf("centerPoint").forGetter(SpiralParticleEffect::centerPoint),
            Codec.FLOAT.fieldOf("angularVelocity").forGetter(SpiralParticleEffect::angularVelocity),
            ParticleTypes.TYPE_CODEC.fieldOf("effect").forGetter(SpiralParticleEffect::effect)
    ).apply(instance, SpiralParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, SpiralParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            WeakTarget.PACKET_CODEC, SpiralParticleEffect::centerPoint,
            PacketCodecs.FLOAT, SpiralParticleEffect::angularVelocity,
            ParticleTypes.PACKET_CODEC, SpiralParticleEffect::effect,
            SpiralParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return UParticles.SPIRAL;
    }
}
