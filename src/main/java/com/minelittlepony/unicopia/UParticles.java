package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.client.particle.ParticleChangelingMagic;
import com.minelittlepony.unicopia.client.particle.ParticleDisk;
import com.minelittlepony.unicopia.client.particle.ParticleRaindrops;
import com.minelittlepony.unicopia.client.particle.ParticleSphere;
import com.minelittlepony.unicopia.client.particle.ParticleUnicornMagic;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.particles.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particles.ParticleTypeRegistry;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;

public class UParticles {
    public static final DefaultParticleType UNICORN_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "unicorn_magic"));
    public static final DefaultParticleType CHANGELING_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "changeling_magic"));

    public static final DefaultParticleType RAIN_DROPS = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "rain_drops"));

    public static final DefaultParticleType SPHERE = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "sphere"));
    public static final DefaultParticleType DISK = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "disk"));

    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(UNICORN_MAGIC, ParticleUnicornMagic::new);
        ParticleFactoryRegistry.getInstance().register(CHANGELING_MAGIC, ParticleChangelingMagic::new);
        ParticleFactoryRegistry.getInstance().register(RAIN_DROPS, ParticleRaindrops::new);
        ParticleFactoryRegistry.getInstance().register(SPHERE, ParticleSphere::new);
        ParticleFactoryRegistry.getInstance().register(DISK, ParticleDisk::new);
    }
}
