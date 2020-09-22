package com.minelittlepony.unicopia.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.network.PacketByteBuf;

public class DiskParticleEffect extends SphereParticleEffect {
    public static final ParticleEffect.Factory<DiskParticleEffect> FACTORY = new ParticleEffect.Factory<DiskParticleEffect>() {
        @Override
        public DiskParticleEffect read(ParticleType<DiskParticleEffect> particleType, StringReader reader) throws CommandSyntaxException {
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
            return new DiskParticleEffect(g, h, i, j, k);
        }

        @Override
        public DiskParticleEffect read(ParticleType<DiskParticleEffect> particleType, PacketByteBuf buf) {
            return new DiskParticleEffect(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };

    public DiskParticleEffect(float red, float green, float blue, float alpha, float rad) {
        super(red, green, blue, alpha, rad);
    }

    @Override
    public ParticleType<?> getType() {
        return UParticles.DISK;
    }
}
