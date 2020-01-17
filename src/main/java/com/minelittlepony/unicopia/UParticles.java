package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.client.particle.ParticleChangelingMagic;
import com.minelittlepony.unicopia.client.particle.ParticleDisk;
import com.minelittlepony.unicopia.client.particle.ParticleRaindrops;
import com.minelittlepony.unicopia.client.particle.ParticleSphere;
import com.minelittlepony.unicopia.client.particle.ParticleUnicornMagic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;

public class UParticles {
    public static final DefaultParticleType UNICORN_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "unicorn_magic"));
    public static final DefaultParticleType CHANGELING_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "changeling_magic"));

    public static final DefaultParticleType RAIN_DROPS = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "rain_drops"));

    public static final DefaultParticleType SPHERE = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "sphere"));
    public static final DefaultParticleType DISK = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "disk"));
}
