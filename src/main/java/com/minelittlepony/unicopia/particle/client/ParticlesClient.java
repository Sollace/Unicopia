package com.minelittlepony.unicopia.particle.client;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.particle.IAttachableParticle;
import com.minelittlepony.unicopia.particle.Particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;

public class ParticlesClient extends Particles<Particle> {

    static final int PARTICLES_ALL = 0;
    static final int PARTICLES_DECREASED = 1;
    static final int PARTICLES_MINIMAL = 2;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public Optional<IAttachableParticle> spawnParticle(int particleId, boolean ignoreDistance, double posX, double posY, double posZ, double speedX, double speedY, double speedZ, int ...pars) {
        Entity entity = mc.getRenderViewEntity();

        if (entity != null && mc.effectRenderer != null) {
            if (ignoreDistance || (entity.getDistanceSq(posX, posY, posZ) <= 1024 && calculateParticleLevel(false) < 2)) {
                return spawnEffectParticle(particleId, posX, posY, posZ, speedX, speedY, speedZ, pars);
            }
        }

        return Optional.empty();
    }

    @Nullable
    private Optional<IAttachableParticle> spawnEffectParticle(int particleId, double posX, double posY, double posZ, double speedX, double speedY, double speedZ, int ...pars) {
        if (particleId >= 0) {
            // Not ours, delegate to mojang
            mc.effectRenderer.spawnEffectParticle(particleId, posX, posY, posZ, speedX, speedY, speedZ, pars);
        } else {
            IFactory<Particle> factory = registeredParticles.get(-(particleId + 1));



            if (factory != null) {
                Particle particle = factory.createParticle(particleId, mc.world, posX, posY, posZ, speedX, speedY, speedZ, pars);

                mc.effectRenderer.addEffect(particle);

                if (particle instanceof IAttachableParticle) {
                    return Optional.ofNullable((IAttachableParticle)particle);
                }
            }
        }

        return Optional.empty();
    }

    private int calculateParticleLevel(boolean minimiseLevel) {
        int level = mc.gameSettings.particleSetting;

        if (minimiseLevel && level == PARTICLES_MINIMAL && mc.world.rand.nextInt(10) == PARTICLES_ALL) {
            level = PARTICLES_DECREASED;
        }

        if (level == PARTICLES_DECREASED && mc.world.rand.nextInt(3) == PARTICLES_ALL) {
            level = PARTICLES_MINIMAL;
        }

        return level;
    }
}
