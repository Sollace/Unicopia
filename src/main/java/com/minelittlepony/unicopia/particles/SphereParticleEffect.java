package com.minelittlepony.unicopia.particles;

import java.util.Locale;

import com.minelittlepony.util.Color;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class SphereParticleEffect implements ParticleEffect {
    public static final ParticleEffect.Factory<SphereParticleEffect> FACTORY = new ParticleEffect.Factory<SphereParticleEffect>() {
        @Override
        public SphereParticleEffect read(ParticleType<SphereParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float g = (float)reader.readDouble();
            reader.expect(' ');
            float h = (float)reader.readDouble();
            reader.expect(' ');
            float i = (float)reader.readDouble();
            reader.expect(' ');
            float j = (float)reader.readDouble();
            reader.expect(' ');
            float k = (float)reader.readDouble();
            return new SphereParticleEffect(g, h, i, j, k);
        }

        @Override
        public SphereParticleEffect read(ParticleType<SphereParticleEffect> particleType, PacketByteBuf buf) {
            return new SphereParticleEffect(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final float radius;

    public SphereParticleEffect(int tint, float alpha, float rad) {
        this(Color.r(tint), Color.g(tint), Color.b(tint), alpha, rad);
    }

    public SphereParticleEffect(float red, float green, float blue, float alpha, float rad) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.radius = rad;
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

    public float getAlpha() {
        return alpha;
    }

    public float getRadius() {
        return radius;
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.SPHERE;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(red);
        buf.writeFloat(green);
        buf.writeFloat(blue);
        buf.writeFloat(alpha);
        buf.writeFloat(radius);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getId(getType()), red, green, blue, alpha, radius);
    }

}
