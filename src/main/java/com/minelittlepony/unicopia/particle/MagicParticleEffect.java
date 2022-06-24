package com.minelittlepony.unicopia.particle;

import java.util.Locale;

import com.minelittlepony.common.util.Color;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;

public class MagicParticleEffect implements ParticleEffect {
    public static final MagicParticleEffect UNICORN = new MagicParticleEffect(false, Vec3f.ZERO);
    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<MagicParticleEffect> FACTORY = ParticleFactoryHelper.of(MagicParticleEffect::new, MagicParticleEffect::new);

    private final boolean tinted;
    private final Vec3f color;

    protected MagicParticleEffect(ParticleType<MagicParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
        this(ParticleFactoryHelper.readBoolean(reader), AbstractDustParticleEffect.readColor(reader));
    }

    protected MagicParticleEffect(ParticleType<MagicParticleEffect> particleType, PacketByteBuf buf) {
        this(buf.readBoolean(), AbstractDustParticleEffect.readColor(buf));
    }

    public MagicParticleEffect(int tint) {
        this(true, new Vec3f(Color.r(tint), Color.g(tint), Color.b(tint)));
    }

    public MagicParticleEffect(Vec3f color) {
        this(true, color);
    }

    protected MagicParticleEffect(boolean tint, Vec3f color) {
        tinted = tint;
        this.color = color;
    }

    public boolean hasTint() {
        return tinted;
    }

    public Vec3f getColor(Random random) {
        if (hasTint()) {
            return color;
        }

        float r = random.nextBoolean() ? 0.9F : 1;
        float g = 0.3F;
        float b = random.nextBoolean() ? 0.4F : 1;

        if (random.nextBoolean()) {
            g *= 2F;
        } else if (random.nextBoolean()) {
            r *= 3.9F;
        }

        return new Vec3f(r, g, b);
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.UNICORN_MAGIC;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(tinted);
        buf.writeFloat(color.getX());
        buf.writeFloat(color.getY());
        buf.writeFloat(color.getZ());
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getId(getType()), color.getX(), color.getY(), color.getZ());
    }

}
