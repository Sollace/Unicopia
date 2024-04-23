package com.minelittlepony.unicopia.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record SpiralParticleEffect(
        WeakTarget centerPoint,
        float angularVelocity,
        ParticleEffect effect
    ) implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<SpiralParticleEffect> FACTORY = ParticleFactoryHelper.of(SpiralParticleEffect::new, SpiralParticleEffect::new);

    protected SpiralParticleEffect(ParticleType<SpiralParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(new WeakTarget(reader),
                ParticleFactoryHelper.readFloat(reader),
                ParticleFactoryHelper.read(reader)
        );
    }

    protected SpiralParticleEffect(ParticleType<SpiralParticleEffect> type, PacketByteBuf buf) {
        this(new WeakTarget(buf),
            buf.readFloat(),
            ParticleFactoryHelper.PARTICLE_EFFECT_CODEC.read(buf)
        );
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.SPIRAL;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        centerPoint.write(buffer);
        buffer.writeFloat(angularVelocity);
        ParticleFactoryHelper.PARTICLE_EFFECT_CODEC.write(buffer, effect);
    }

    @Override
    public String asString() {
        return null;
    }
}
