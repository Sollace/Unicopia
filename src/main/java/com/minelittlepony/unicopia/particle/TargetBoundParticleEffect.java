package com.minelittlepony.unicopia.particle;


import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record TargetBoundParticleEffect (
        ParticleType<TargetBoundParticleEffect> type,
        int targetId
    ) implements ParticleEffect {
    public static MapCodec<TargetBoundParticleEffect> createCodec(ParticleType<TargetBoundParticleEffect> type) {
        return Codec.INT.fieldOf("targetId").xmap(targetId -> new TargetBoundParticleEffect(type, targetId), TargetBoundParticleEffect::targetId);
    }

    public static final PacketCodec<ByteBuf, TargetBoundParticleEffect> createPacketCodec(ParticleType<TargetBoundParticleEffect> type) {
        return PacketCodecs.INTEGER.xmap(targetId -> new TargetBoundParticleEffect(type, targetId), TargetBoundParticleEffect::targetId);
    }

    public TargetBoundParticleEffect(ParticleType<TargetBoundParticleEffect> type, @Nullable Entity target) {
        this(type, target == null ? -1 : target.getId());
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
