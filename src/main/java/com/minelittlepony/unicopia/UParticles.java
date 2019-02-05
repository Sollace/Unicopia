package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.particle.client.ParticleUnicornMagic;
import com.minelittlepony.unicopia.particle.client.ParticleRaindrops;
import com.minelittlepony.unicopia.particle.client.ParticleSphere;
import com.minelittlepony.unicopia.particle.client.ParticleChanglingMagic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class UParticles {

    public static int UNICORN_MAGIC;
    public static int CHANGELING_MAGIC;

    public static int RAIN_DROPS;

    public static int SPHERE;

    @SideOnly(Side.CLIENT)
    static void init() {
        UNICORN_MAGIC = Particles.instance().registerParticle(ParticleUnicornMagic::new);
        RAIN_DROPS = Particles.instance().registerParticle(ParticleRaindrops::new);
        CHANGELING_MAGIC = Particles.instance().registerParticle(ParticleChanglingMagic::new);
        SPHERE = Particles.instance().registerParticle(ParticleSphere::new);
    }
}
