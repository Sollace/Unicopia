package com.minelittlepony.unicopia.util.particles;

import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;

public interface UParticles {
    // TODO: ParticleTypeRegistry
    interface ParticleTypeRegistry {
        static ParticleTypeRegistry getTnstance() {return null;}
        DefaultParticleType register(Identifier id);
    }
    DefaultParticleType UNICORN_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "unicorn_magic"));
    DefaultParticleType CHANGELING_MAGIC = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "changeling_magic"));

    DefaultParticleType RAIN_DROPS = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "rain_drops"));

    DefaultParticleType SPHERE = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "sphere"));
    DefaultParticleType DISK = ParticleTypeRegistry.getTnstance().register(new Identifier("unicopia", "disk"));
}
