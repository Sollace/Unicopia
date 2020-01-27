package com.minelittlepony.unicopia.core;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;

public class UParticles {
    // TODO:
    interface ParticleTypeRegistry {
        static ParticleTypeRegistry getTnstance() {return null;}
        DefaultParticleType register(Identifier id);
    }
    public static final DefaultParticleType UNICORN_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "unicorn_magic"));
    public static final DefaultParticleType CHANGELING_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "changeling_magic"));

    public static final DefaultParticleType RAIN_DROPS = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "rain_drops"));

    public static final DefaultParticleType SPHERE = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "sphere"));
    public static final DefaultParticleType DISK = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "disk"));
}
