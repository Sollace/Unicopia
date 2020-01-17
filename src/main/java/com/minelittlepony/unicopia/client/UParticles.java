package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.client.particle.ParticleChangelingMagic;
import com.minelittlepony.unicopia.client.particle.ParticleDisk;
import com.minelittlepony.unicopia.client.particle.ParticleRaindrops;
import com.minelittlepony.unicopia.client.particle.ParticleSphere;
import com.minelittlepony.unicopia.client.particle.ParticleUnicornMagic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;

class UParticles extends com.minelittlepony.unicopia.UParticles {
    static void bootstrap() {
        ParticleFactoryRegistry.instance().register(UNICORN_MAGIC, ParticleUnicornMagic::new);
        ParticleFactoryRegistry.instance().register(CHANGELING_MAGIC, ParticleChangelingMagic::new);
        ParticleFactoryRegistry.instance().register(RAIN_DROPS, ParticleRaindrops::new);
        ParticleFactoryRegistry.instance().register(SPHERE, ParticleSphere::new);
        ParticleFactoryRegistry.instance().register(DISK, ParticleDisk::new);
    }
}
