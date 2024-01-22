package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

public class CloudsEscapingParticle extends GroundPoundParticle {

    public CloudsEscapingParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z, double dX, double dY, double dZ) {
        super(effect, world, x, y, z, dX, dY, dZ);
        maxAge = 200;
        collidesWithWorld = false;
    }

    @Override
    public void spawnChildParticles() {
        this.velocityY += 0.125;

        Vec3d center = getPos();

        double variance = age / maxAge;
        Vec3d vel = new Vec3d(
                (world.random.nextFloat() - 0.5) * (0.125 + variance),
                velocityY * 0.2,
                (world.random.nextFloat() - 0.5) * (0.125 + variance)
        );

        double columnHeight = 1 + age / 30;
        new Sphere(true, columnHeight)
            .translate(center)
            .randomPoints(random)
            .forEach(point -> {
                ParticleUtils.spawnParticle(world, ParticleTypes.CLOUD, point, vel);
        });
    }
}
