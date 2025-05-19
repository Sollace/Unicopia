package com.minelittlepony.unicopia.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

public record OrientedBillboardParticleEffect (
        ParticleType<OrientedBillboardParticleEffect> type,
        boolean fixed,
        float yaw,
        float pitch
    ) implements ParticleEffect {
    public static MapCodec<OrientedBillboardParticleEffect> createCodec(ParticleType<OrientedBillboardParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("fixed").forGetter(OrientedBillboardParticleEffect::fixed),
            Codec.FLOAT.fieldOf("yaw").forGetter(OrientedBillboardParticleEffect::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(OrientedBillboardParticleEffect::pitch)
        ).apply(instance, (fixed, yaw, pitch) -> new OrientedBillboardParticleEffect(type, fixed, yaw, pitch)));
    }

    public static final PacketCodec<RegistryByteBuf, OrientedBillboardParticleEffect> createPacketCodec(ParticleType<OrientedBillboardParticleEffect> type) {
        return PacketCodec.tuple(
                PacketCodecs.BOOL, OrientedBillboardParticleEffect::fixed,
                PacketCodecs.FLOAT, OrientedBillboardParticleEffect::yaw,
                PacketCodecs.FLOAT, OrientedBillboardParticleEffect::pitch,
                (fixed, yaw, pitch) -> new OrientedBillboardParticleEffect(type, fixed, yaw, pitch)
        );
    }

    public OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, Vec3d orientation) {
        this(type, (float)orientation.getX(), (float)orientation.getY());
    }

    public OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, float yaw, float pitch) {
        this(type, true, yaw, pitch);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
