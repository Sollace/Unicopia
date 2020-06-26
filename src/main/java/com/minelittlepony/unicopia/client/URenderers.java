package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.client.particle.DiskParticle;
import com.minelittlepony.unicopia.client.particle.MagicParticle;
import com.minelittlepony.unicopia.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.client.particle.SphereParticle;
import com.minelittlepony.unicopia.particles.UParticles;
import com.minelittlepony.unicopia.world.client.UWorldClient;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public interface URenderers {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(UParticles.UNICORN_MAGIC, MagicParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.CHANGELING_MAGIC, ChangelingMagicParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.RAIN_DROPS, RaindropsParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.SPHERE, SphereParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.DISK, DiskParticle::new);

        UWorldClient.bootstrap();
    }
}
