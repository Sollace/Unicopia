package com.minelittlepony.unicopia.particle;

import java.util.Optional;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
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
    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<LightningBoltParticleEffect> FACTORY = ParticleFactoryHelper.of(LightningBoltParticleEffect::new, LightningBoltParticleEffect::new);

    protected LightningBoltParticleEffect(ParticleType<LightningBoltParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
        this(
                ParticleFactoryHelper.readBoolean(reader),
                ParticleFactoryHelper.readInt(reader),
                ParticleFactoryHelper.readInt(reader),
                ParticleFactoryHelper.readFloat(reader),
                ParticleFactoryHelper.readOptional(reader, ParticleFactoryHelper::readVector)
        );
    }

    protected LightningBoltParticleEffect(ParticleType<LightningBoltParticleEffect> particleType, PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readFloat(), ParticleFactoryHelper.OPTIONAL_VECTOR_CODEC.read(buf));
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.LIGHTNING_BOLT;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeBoolean(silent);
        buffer.writeInt(changeFrequency);
        buffer.writeInt(maxBranches);
        buffer.writeFloat(maxDeviation);
        ParticleFactoryHelper.OPTIONAL_VECTOR_CODEC.write(buffer, pathEndPoint);
    }

    @Override
    public String asString() {
        return String.format("%s %s %s %s", silent, changeFrequency, maxBranches, maxDeviation);
    }

}
