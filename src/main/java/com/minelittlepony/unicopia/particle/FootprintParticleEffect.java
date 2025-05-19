package com.minelittlepony.unicopia.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record FootprintParticleEffect (
        float yaw
    ) implements ParticleEffect {
    public static final MapCodec<FootprintParticleEffect> CODEC = Codec.FLOAT.fieldOf("yaw").xmap(FootprintParticleEffect::new, FootprintParticleEffect::yaw);
    public static final PacketCodec<ByteBuf, FootprintParticleEffect> PACKET_CODEC = PacketCodecs.FLOAT.xmap(FootprintParticleEffect::new, FootprintParticleEffect::yaw);

    @Override
    public ParticleType<?> getType() {
        return UParticles.FOOTPRINT;
    }
}
