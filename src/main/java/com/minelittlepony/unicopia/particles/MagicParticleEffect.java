package com.minelittlepony.unicopia.particles;

import java.util.Locale;

import com.minelittlepony.util.Color;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class MagicParticleEffect implements ParticleEffect {

    public static final MagicParticleEffect UNICORN = new MagicParticleEffect(false, 0, 0, 0);
    public static final ParticleEffect.Factory<MagicParticleEffect> UNICORN_FACTORY = new ParticleEffect.Factory<MagicParticleEffect>() {
        @Override
        public MagicParticleEffect read(ParticleType<MagicParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            boolean f = reader.readBoolean();
            reader.expect(' ');
            float g = (float)reader.readDouble();
            reader.expect(' ');
            float h = (float)reader.readDouble();
            reader.expect(' ');
            float i = (float)reader.readDouble();
            return new MagicParticleEffect(f, g, h, i);
        }

        @Override
        public MagicParticleEffect read(ParticleType<MagicParticleEffect> particleType, PacketByteBuf buf) {
            return new MagicParticleEffect(buf.readBoolean(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };

    private final boolean tinted;
    private final float red;
    private final float green;
    private final float blue;

    public MagicParticleEffect(int tint) {
        this(true, Color.r(tint), Color.g(tint), Color.b(tint));
    }

    public MagicParticleEffect(float r, float g, float b) {
        this(true, r, g, b);
    }

    protected MagicParticleEffect(boolean tint, float r, float g, float b) {
        tinted = tint;
        red = r;
        green = g;
        blue = b;
    }


    public boolean hasTint() {
        return tinted;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.UNICORN_MAGIC;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(tinted);
        buf.writeFloat(red);
        buf.writeFloat(green);
        buf.writeFloat(blue);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getId(getType()), red, green, blue);
    }

}
