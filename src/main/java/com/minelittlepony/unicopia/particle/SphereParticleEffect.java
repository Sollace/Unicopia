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
import net.minecraft.util.registry.Registry;

public class SphereParticleEffect implements ParticleEffect {
    public static final Factory<SphereParticleEffect> FACTORY = ParticleFactoryHelper.of(SphereParticleEffect::new, SphereParticleEffect::new);

    private final Vec3f color;
    private final float alpha;
    private final float radius;

    protected SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(AbstractDustParticleEffect.readColor(reader), ParticleFactoryHelper.readFloat(reader), ParticleFactoryHelper.readFloat(reader));
    }

    protected SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, PacketByteBuf buf) {
        this(AbstractDustParticleEffect.readColor(buf), buf.readFloat(), buf.readFloat());
    }

    public SphereParticleEffect(int tint, float alpha, float rad) {
        this(new Vec3f(Color.r(tint), Color.g(tint), Color.b(tint)), alpha, rad);
    }

    public SphereParticleEffect(Vec3f color, float alpha, float rad) {
        this.color = color;
        this.alpha = alpha;
        this.radius = rad;
    }

    public Vec3f getColor() {
        return color;
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
        buf.writeFloat(color.getX());
        buf.writeFloat(color.getY());
        buf.writeFloat(color.getZ());
        buf.writeFloat(alpha);
        buf.writeFloat(radius);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", Registry.PARTICLE_TYPE.getId(getType()), color.getX(), color.getY(), color.getZ(), alpha, radius);
    }
}
