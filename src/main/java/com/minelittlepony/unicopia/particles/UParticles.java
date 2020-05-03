package com.minelittlepony.unicopia.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UParticles {

    ParticleType<MagicParticleEffect> UNICORN_MAGIC = register("unicorn_magic", FabricParticleTypes.complex(MagicParticleEffect.UNICORN_FACTORY));
    DefaultParticleType CHANGELING_MAGIC = register("changeling_magic", FabricParticleTypes.simple());

    DefaultParticleType RAIN_DROPS = register("rain_drops", FabricParticleTypes.simple());

    ParticleType<SphereParticleEffect> SPHERE = register("sphere", FabricParticleTypes.complex(true, SphereParticleEffect.FACTORY));
    ParticleType<DiskParticleEffect> DISK = register("disk", FabricParticleTypes.complex(true, DiskParticleEffect.FACTORY));

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registry.PARTICLE_TYPE, new Identifier("unicopia", name), type);
    }
}
