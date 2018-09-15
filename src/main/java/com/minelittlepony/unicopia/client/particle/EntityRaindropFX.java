package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRain;
import net.minecraft.world.World;

public class EntityRaindropFX extends ParticleRain {

	public EntityRaindropFX(World w, double x, double y, double z) {
		super(w, x, y, z);
		motionY = -0.1;
		particleMaxAge += 19;
    }

	public void onUpdate() {
	    super.onUpdate();

	    if (onGround) {
            motionX *= 0.30000001192092896D;
            motionY = Math.random() * 0.20000000298023224D + 0.10000000149011612D;
            motionZ *= 0.30000001192092896D;
        }
    }

	public static class Factory implements IParticleFactory {
		@Override
		public Particle createParticle(int id, World w, double x, double y, double z, double vX, double vY, double vZ, int... args) {
			return new EntityRaindropFX(w, x, y, z);
		}

	}
}
