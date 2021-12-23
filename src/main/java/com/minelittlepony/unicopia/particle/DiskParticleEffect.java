package com.minelittlepony.unicopia.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3f;

public class DiskParticleEffect extends SphereParticleEffect {
    @SuppressWarnings("deprecation")
    public static final Factory<DiskParticleEffect> FACTORY = ParticleFactoryHelper.of(DiskParticleEffect::new, DiskParticleEffect::new);

    protected DiskParticleEffect(ParticleType<DiskParticleEffect> type, StringReader reader) throws CommandSyntaxException {
        super(type, reader);
    }

    protected DiskParticleEffect(ParticleType<DiskParticleEffect> type, PacketByteBuf buf) {
        super(type, buf);
    }

    public DiskParticleEffect(Vec3f color, float alpha, float rad) {
        super(color, alpha, rad);
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.DISK;
    }
}
