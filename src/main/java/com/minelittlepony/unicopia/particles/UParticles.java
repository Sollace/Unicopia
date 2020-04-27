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

    DefaultParticleType SPHERE = register("sphere", FabricParticleTypes.simple(true));
    DefaultParticleType DISK = register("disk", FabricParticleTypes.simple());

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registry.PARTICLE_TYPE, new Identifier("unicopia", name), type);
    }
}
