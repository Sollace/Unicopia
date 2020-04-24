package com.minelittlepony.unicopia.particles;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

public interface ParticleSpawner {
    void addParticle(ParticleEffect effect, Vec3d position, Vec3d velocity);
}
