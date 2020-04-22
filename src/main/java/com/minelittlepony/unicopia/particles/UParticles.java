package com.minelittlepony.unicopia.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface UParticles {

    ParticleType<MagicParticleEffect> UNICORN_MAGIC = register(FabricParticleTypes.complex(MagicParticleEffect.UNICORN_FACTORY), "unicorn_magic");
    DefaultParticleType CHANGELING_MAGIC = register(FabricParticleTypes.simple(), "changeling_magic");

    DefaultParticleType RAIN_DROPS = register(FabricParticleTypes.simple(), "rain_drops");

    DefaultParticleType SPHERE = register(FabricParticleTypes.simple(true), "sphere");
    DefaultParticleType DISK = register(FabricParticleTypes.simple(), "disk");

    static <T extends ParticleType<?>> T register(T type, String name) {
        return Registry.register(Registry.PARTICLE_TYPE, new Identifier("unicopia", name), type);
    }
}
