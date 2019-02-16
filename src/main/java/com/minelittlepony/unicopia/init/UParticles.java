package com.minelittlepony.unicopia.init;

import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.particle.client.ParticleUnicornMagic;
import com.minelittlepony.unicopia.particle.client.ParticleRaindrops;
import com.minelittlepony.unicopia.particle.client.ParticleSphere;
import com.minelittlepony.unicopia.particle.client.ParticleChanglingMagic;
import com.minelittlepony.unicopia.particle.client.ParticleDisk;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UParticles {

    public static int UNICORN_MAGIC;
    public static int CHANGELING_MAGIC;

    public static int RAIN_DROPS;

    public static int SPHERE;
    public static int DISK;

    @SideOnly(Side.CLIENT)
    public static void init() {
        UNICORN_MAGIC = Particles.instance().registerParticle(ParticleUnicornMagic::new);
        RAIN_DROPS = Particles.instance().registerParticle(ParticleRaindrops::new);
        CHANGELING_MAGIC = Particles.instance().registerParticle(ParticleChanglingMagic::new);
        SPHERE = Particles.instance().registerParticle(ParticleSphere::new);
        DISK = Particles.instance().registerParticle(ParticleDisk::new);
    }
}
