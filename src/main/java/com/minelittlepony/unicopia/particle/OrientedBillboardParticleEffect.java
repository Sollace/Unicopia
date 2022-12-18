package com.minelittlepony.unicopia.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.Registries;

public class OrientedBillboardParticleEffect implements ParticleEffect {
    @SuppressWarnings("deprecation")
    public static final ParticleEffect.Factory<OrientedBillboardParticleEffect> FACTORY = ParticleFactoryHelper.of(OrientedBillboardParticleEffect::new, OrientedBillboardParticleEffect::new);

    private final boolean fixed;
    private final float yaw;
    private final float pitch;

    private final ParticleType<OrientedBillboardParticleEffect> type;

    protected OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        this(type, ParticleFactoryHelper.readBoolean(reader), ParticleFactoryHelper.readFloat(reader), ParticleFactoryHelper.readFloat(reader));
    }

    protected OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> particleType, PacketByteBuf buf) {
        this(particleType, buf.readBoolean(), buf.readFloat(), buf.readFloat());
    }

    public OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, Vec3d orientation) {
        this(type, (float)orientation.getX(), (float)orientation.getY());
    }

    public OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, float yaw, float pitch) {
        this(type, true, yaw, pitch);
    }

    private OrientedBillboardParticleEffect(ParticleType<OrientedBillboardParticleEffect> type, boolean fixed, float yaw, float pitch) {
        this.fixed = fixed;
        this.yaw = yaw;
        this.pitch = pitch;
        this.type = type;
    }

    public boolean isAngleFixed() {
        return fixed;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(fixed);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    public String asString() {
        return String.format(Locale.ROOT, "%s %b %.2f %.2f", Registries.PARTICLE_TYPE.getId(getType()), fixed, yaw, pitch);
    }

}
