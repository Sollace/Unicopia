package com.minelittlepony.unicopia.particle;

import java.util.Locale;

import com.minelittlepony.common.util.Color;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;

public class SphereParticleEffect implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<SphereParticleEffect> FACTORY = ParticleFactoryHelper.of(SphereParticleEffect::new, SphereParticleEffect::new);

    private static final Vec3d DEFAULT_OFFSET = new Vec3d(0, 0.5, 0);

    private final Vec3f color;
    private final float alpha;
    private final float radius;

    private Vec3d offset = Vec3d.ZERO;

    protected SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(AbstractDustParticleEffect.readColor(reader), ParticleFactoryHelper.readFloat(reader), ParticleFactoryHelper.readFloat(reader), ParticleFactoryHelper.readVector(reader));
    }

    protected SphereParticleEffect(ParticleType<? extends SphereParticleEffect> type, PacketByteBuf buf) {
        this(AbstractDustParticleEffect.readColor(buf), buf.readFloat(), buf.readFloat());
    }

    public SphereParticleEffect(int tint, float alpha, float rad) {
        this(tint, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(Vec3f color, float alpha, float rad) {
        this(color, alpha, rad, DEFAULT_OFFSET);
    }

    public SphereParticleEffect(int tint, float alpha, float rad, Vec3d offset) {
        this(new Vec3f(Color.r(tint), Color.g(tint), Color.b(tint)), alpha, rad, offset);
    }

    public SphereParticleEffect(Vec3f color, float alpha, float rad, Vec3d offset) {
        this.color = color;
        this.offset = offset;
        this.alpha = alpha;
        this.radius = rad;
    }

    public Vec3d getOffset() {
        return offset;
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
        buf.writeDouble(offset.getX());
        buf.writeDouble(offset.getY());
        buf.writeDouble(offset.getZ());
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f %.2f %.2f %.2f",
                Registry.PARTICLE_TYPE.getId(getType()),
                color.getX(), color.getY(), color.getZ(),
                alpha,
                radius,
                offset.getX(), offset.getY(), offset.getZ()
        );
    }
}
