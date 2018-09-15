package com.minelittlepony.unicopia.client.particle;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;

public class Particles {

    private static final Particles instance = new Particles();

    public static Particles instance() {
        return instance;
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    private final List<IParticleFactory> registeredParticles = new ArrayList<>();

    private final EntityParticleEmitter entityEmitter = new EntityParticleEmitter();

    public EntityParticleEmitter getEntityEmitter() {
        return entityEmitter;
    }

    public int registerParticle(IParticleFactory factory) {
        int id = registeredParticles.size();
        registeredParticles.add(factory);
        return -id - 1;
    }

    public Particle spawnParticle(int particleId, boolean ignoreDistance, double posX, double posY, double posZ, double speedX, double speedY, double speedZ, int ...pars) {
        Entity entity = mc.getRenderViewEntity();

        if (entity == null && mc.effectRenderer == null) {
            return null;
        }

        double dX = entity.posX - posX;
        double dY = entity.posY - posY;
        double dZ = entity.posZ - posZ;
        double distance = dX * dX + dY * dY + dZ * dZ;

        if (ignoreDistance || (distance <= 1024 && calculateParticleLevel(false) < 2)) {
            return spawnEffectParticle(particleId, posX, posY, posZ, speedX, speedY, speedZ, pars);
        }

        return null;
    }

    private Particle spawnEffectParticle(int particleId, double posX, double posY, double posZ, double speedX, double speedY, double speedZ, int ...pars) {
        if (particleId >= 0) {
            // Not ours, delegate to mojang
            return mc.effectRenderer.spawnEffectParticle(particleId, posX, posY, posZ, speedX, speedY, speedZ, pars);
        }

        particleId ++;

        IParticleFactory iparticlefactory = registeredParticles.get(-particleId);

        if (iparticlefactory == null) {
            return null;
        }

        Particle particle = iparticlefactory.createParticle(particleId, mc.world, posX, posY, posZ, speedX, speedY, speedZ, pars);

        if (particle == null) {
            return null;
        }

        mc.effectRenderer.addEffect(particle);
        return particle;
    }

    private int calculateParticleLevel(boolean minimiseLevel) {
        int level = mc.gameSettings.particleSetting;

        if (minimiseLevel && level == 2 && mc.world.rand.nextInt(10) == 0) {
            level = 1;
        }

        if (level == 1 && mc.world.rand.nextInt(3) == 0) {
            level = 2;
        }

        return level;
    }
}
