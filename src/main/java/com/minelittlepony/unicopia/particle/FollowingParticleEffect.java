package com.minelittlepony.unicopia.particle;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

public record FollowingParticleEffect (
        ParticleType<FollowingParticleEffect> type,
        WeakTarget target,
        float followSpeed,
        Optional<ParticleEffect> childEffect
    ) implements ParticleEffect {
    public static MapCodec<FollowingParticleEffect> createCodec(ParticleType<FollowingParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            WeakTarget.CODEC.fieldOf("target").forGetter(FollowingParticleEffect::target),
            Codec.FLOAT.fieldOf("follow_speed").forGetter(FollowingParticleEffect::followSpeed),
            ParticleTypes.TYPE_CODEC.optionalFieldOf("child_effect").forGetter(FollowingParticleEffect::childEffect)
        ).apply(instance, (target, speed, effect) -> new FollowingParticleEffect(type, target, speed, effect)));
    }

    public static final PacketCodec<RegistryByteBuf, FollowingParticleEffect> createPacketCodec(ParticleType<FollowingParticleEffect> type) {
        return PacketCodec.tuple(
                WeakTarget.PACKET_CODEC, FollowingParticleEffect::target,
                PacketCodecs.FLOAT, FollowingParticleEffect::followSpeed,
                PacketCodecs.optional(ParticleTypes.PACKET_CODEC), FollowingParticleEffect::childEffect,
                (target, speed, effect) -> new FollowingParticleEffect(type, target, speed, effect)
        );
    }

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Vec3d target, float followSpeed) {
        this(type, new WeakTarget(target, (Entity)null), followSpeed, Optional.empty());
    }

    public FollowingParticleEffect(ParticleType<FollowingParticleEffect> type, Entity target, float followSpeed) {
        this(type, new WeakTarget(target.getCameraPosVec(1), target), followSpeed, Optional.empty());
    }

    public FollowingParticleEffect withChild(ParticleEffect child) {
        return new FollowingParticleEffect(type, target, followSpeed, Optional.of(child));
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
