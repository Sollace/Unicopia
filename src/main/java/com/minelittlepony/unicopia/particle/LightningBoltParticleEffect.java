package com.minelittlepony.unicopia.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record LightningBoltParticleEffect (
        boolean silent,
        int changeFrequency,
        int maxBranchLength
    ) implements ParticleEffect {
    public static final LightningBoltParticleEffect DEFAULT = new LightningBoltParticleEffect(false, 10, 6);
    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<LightningBoltParticleEffect> FACTORY = ParticleFactoryHelper.of(LightningBoltParticleEffect::new, LightningBoltParticleEffect::new);

    protected LightningBoltParticleEffect(ParticleType<LightningBoltParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
        this(ParticleFactoryHelper.readBoolean(reader), ParticleFactoryHelper.readInt(reader), ParticleFactoryHelper.readInt(reader));
    }

    protected LightningBoltParticleEffect(ParticleType<LightningBoltParticleEffect> particleType, PacketByteBuf buf) {
        this(buf.readBoolean(), buf.readInt(), buf.readInt());
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.LIGHTNING_BOLT;
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeBoolean(silent);
        buffer.writeInt(changeFrequency);
        buffer.writeInt(maxBranchLength);
    }

    @Override
    public String asString() {
        return String.format("%s %s %s", silent, changeFrequency, maxBranchLength);
    }

}
