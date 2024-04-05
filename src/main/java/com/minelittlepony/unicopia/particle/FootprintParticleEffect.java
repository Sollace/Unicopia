package com.minelittlepony.unicopia.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;

public record FootprintParticleEffect (
        float yaw
    ) implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<FootprintParticleEffect> FACTORY = ParticleFactoryHelper.of(FootprintParticleEffect::new, FootprintParticleEffect::new);

    protected FootprintParticleEffect(ParticleType<FootprintParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(ParticleFactoryHelper.readFloat(reader));
    }

    protected FootprintParticleEffect(ParticleType<FootprintParticleEffect> particleType, PacketByteBuf buf) {
        this(buf.readFloat());
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.FOOTPRINT;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(yaw);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f", Registries.PARTICLE_TYPE.getId(getType()), yaw);
    }

}
