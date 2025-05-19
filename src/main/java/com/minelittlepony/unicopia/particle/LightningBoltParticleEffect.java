package com.minelittlepony.unicopia.particle;

import java.util.Optional;

import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public record LightningBoltParticleEffect (
        boolean silent,
        int changeFrequency,
        int maxBranches,
        float maxDeviation,
        Optional<Vec3d> pathEndPoint
    ) implements ParticleEffect {
    public static final LightningBoltParticleEffect DEFAULT = new LightningBoltParticleEffect(false, 10, 6, 3, Optional.empty());

    public static final MapCodec<LightningBoltParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("silent").forGetter(LightningBoltParticleEffect::silent),
            Codec.INT.fieldOf("changeFrequency").forGetter(LightningBoltParticleEffect::changeFrequency),
            Codec.INT.fieldOf("maxBranches").forGetter(LightningBoltParticleEffect::maxBranches),
            Codec.FLOAT.fieldOf("maxDeviation").forGetter(LightningBoltParticleEffect::maxDeviation),
            CodecUtils.VECTOR.optionalFieldOf("pathEndPoint").forGetter(LightningBoltParticleEffect::pathEndPoint)
    ).apply(instance, LightningBoltParticleEffect::new));
    public static final PacketCodec<RegistryByteBuf, LightningBoltParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, LightningBoltParticleEffect::silent,
            PacketCodecs.INTEGER, LightningBoltParticleEffect::changeFrequency,
            PacketCodecs.INTEGER, LightningBoltParticleEffect::maxBranches,
            PacketCodecs.FLOAT, LightningBoltParticleEffect::maxDeviation,
            PacketCodecUtils.OPTIONAL_VECTOR, LightningBoltParticleEffect::pathEndPoint,
            LightningBoltParticleEffect::new
    );

    @Override
    public ParticleType<?> getType() {
        return UParticles.LIGHTNING_BOLT;
    }
}
