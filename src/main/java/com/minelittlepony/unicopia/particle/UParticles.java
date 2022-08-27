package com.minelittlepony.unicopia.particle;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

public interface UParticles {

    ParticleType<MagicParticleEffect> UNICORN_MAGIC = register("unicorn_magic", FabricParticleTypes.complex(MagicParticleEffect.FACTORY));
    DefaultParticleType CHANGELING_MAGIC = register("changeling_magic", FabricParticleTypes.simple());

    ParticleType<OrientedBillboardParticleEffect> RAINBOOM_RING = register("rainboom_ring", FabricParticleTypes.complex(OrientedBillboardParticleEffect.FACTORY));
    DefaultParticleType RAINBOOM_TRAIL = register("rainboom_trail", FabricParticleTypes.simple());

    ParticleType<OrientedBillboardParticleEffect> MAGIC_RUNES = register("magic_runes", FabricParticleTypes.complex(OrientedBillboardParticleEffect.FACTORY));

    DefaultParticleType RAIN_DROPS = register("rain_drops", FabricParticleTypes.simple());

    ParticleType<SphereParticleEffect> SPHERE = register("sphere", FabricParticleTypes.complex(true, SphereParticleEffect.FACTORY));
    ParticleType<SphereParticleEffect> DISK = register("disk", FabricParticleTypes.complex(true, SphereParticleEffect.FACTORY));

    ParticleType<FollowingParticleEffect> HEALTH_DRAIN = register("health_drain", FabricParticleTypes.complex(true, FollowingParticleEffect.FACTORY));

    DefaultParticleType GROUND_POUND = register("ground_pound", FabricParticleTypes.simple());
    DefaultParticleType CLOUDS_ESCAPING = register("clouds_escaping", FabricParticleTypes.simple(true));

    DefaultParticleType LIGHTNING_BOLT = register("lightning_bolt", FabricParticleTypes.simple(true));

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registry.PARTICLE_TYPE, Unicopia.id(name), type);
    }

    static void bootstrap() {}
}
