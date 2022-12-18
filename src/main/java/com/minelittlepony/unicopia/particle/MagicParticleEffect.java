package com.minelittlepony.unicopia.particle;

import java.util.Locale;

import org.joml.Vector3f;

import com.minelittlepony.common.util.Color;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.Registries;

public class MagicParticleEffect implements ParticleEffect {
    public static final MagicParticleEffect UNICORN = new MagicParticleEffect(false, new Vector3f());
    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<MagicParticleEffect> FACTORY = ParticleFactoryHelper.of(MagicParticleEffect::new, MagicParticleEffect::new);

    private final boolean tinted;
    private final Vector3f color;

    protected MagicParticleEffect(ParticleType<MagicParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
        this(ParticleFactoryHelper.readBoolean(reader), AbstractDustParticleEffect.readColor(reader));
    }

    protected MagicParticleEffect(ParticleType<MagicParticleEffect> particleType, PacketByteBuf buf) {
        this(buf.readBoolean(), AbstractDustParticleEffect.readColor(buf));
    }

    public MagicParticleEffect(int tint) {
        this(true, new Vector3f(Color.r(tint), Color.g(tint), Color.b(tint)));
    }

    public MagicParticleEffect(Vector3f color) {
        this(true, color);
    }

    protected MagicParticleEffect(boolean tint, Vector3f color) {
        tinted = tint;
        this.color = color;
    }

    public boolean hasTint() {
        return tinted;
    }

    public Vector3f getColor(Random random) {
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

        return new Vector3f(r, g, b);
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.UNICORN_MAGIC;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(tinted);
        buf.writeFloat(color.x);
        buf.writeFloat(color.y);
        buf.writeFloat(color.z);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", Registries.PARTICLE_TYPE.getId(getType()), color.x, color.y, color.z);
    }

}
