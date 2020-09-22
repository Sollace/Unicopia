package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.UEntities;
import com.minelittlepony.unicopia.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.client.particle.DiskParticle;
import com.minelittlepony.unicopia.client.particle.MagicParticle;
import com.minelittlepony.unicopia.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.client.particle.SphereParticle;
import com.minelittlepony.unicopia.particle.UParticles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public interface URenderers {
    static void bootstrap() {
        ParticleFactoryRegistry.getInstance().register(UParticles.UNICORN_MAGIC, MagicParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.CHANGELING_MAGIC, ChangelingMagicParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.RAIN_DROPS, RaindropsParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.SPHERE, SphereParticle::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.DISK, DiskParticle::new);

        EntityRendererRegistry.INSTANCE.register(UEntities.THROWN_ITEM, (manager, context) -> new FlyingItemEntityRenderer<>(manager, context.getItemRenderer()));
    }
}
