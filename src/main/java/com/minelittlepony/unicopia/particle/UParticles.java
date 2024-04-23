package com.minelittlepony.unicopia.particle;

import com.minelittlepony.unicopia.Unicopia;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;

public interface UParticles {

    ParticleType<MagicParticleEffect> UNICORN_MAGIC = register("unicorn_magic", FabricParticleTypes.complex(MagicParticleEffect.FACTORY));
    DefaultParticleType CHANGELING_MAGIC = register("changeling_magic", FabricParticleTypes.simple());
    DefaultParticleType BUBBLE = register("bubble", FabricParticleTypes.simple());
    ParticleType<FootprintParticleEffect> FOOTPRINT = register("footprint", FabricParticleTypes.complex(FootprintParticleEffect.FACTORY));
    ParticleType<BlockStateParticleEffect> DUST_CLOUD = register("dust_cloud", FabricParticleTypes.complex(BlockStateParticleEffect.PARAMETERS_FACTORY));

    ParticleType<OrientedBillboardParticleEffect> RAINBOOM_RING = register("rainboom_ring", FabricParticleTypes.complex(OrientedBillboardParticleEffect.FACTORY));
    ParticleType<TargetBoundParticleEffect> RAINBOOM_TRAIL = register("rainboom_trail", FabricParticleTypes.complex(TargetBoundParticleEffect.FACTORY));
    ParticleType<TargetBoundParticleEffect> WIND = register("wind", FabricParticleTypes.complex(TargetBoundParticleEffect.FACTORY));

    DefaultParticleType RAIN_DROPS = register("rain_drops", FabricParticleTypes.simple());

    ParticleType<SphereParticleEffect> SPHERE = register("sphere", FabricParticleTypes.complex(true, SphereParticleEffect.FACTORY));
    ParticleType<SphereParticleEffect> DISK = register("disk", FabricParticleTypes.complex(true, SphereParticleEffect.FACTORY));

    ParticleType<FollowingParticleEffect> HEALTH_DRAIN = register("health_drain", FabricParticleTypes.complex(true, FollowingParticleEffect.FACTORY));
    ParticleType<SpiralParticleEffect> SPIRAL = register("spiral", FabricParticleTypes.complex(true, SpiralParticleEffect.FACTORY));

    DefaultParticleType GROUND_POUND = register("ground_pound", FabricParticleTypes.simple());
    DefaultParticleType CLOUDS_ESCAPING = register("clouds_escaping", FabricParticleTypes.simple(true));

    ParticleType<LightningBoltParticleEffect> LIGHTNING_BOLT = register("lightning_bolt", FabricParticleTypes.complex(true, LightningBoltParticleEffect.FACTORY));
    DefaultParticleType SHOCKWAVE = register("shockwave", FabricParticleTypes.simple(true));

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(Registries.PARTICLE_TYPE, Unicopia.id(name), type);
    }

    static void bootstrap() {}
}
