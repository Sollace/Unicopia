package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.client.particle.ChangelingMagicParticle;
import com.minelittlepony.unicopia.client.particle.DiskParticle;
import com.minelittlepony.unicopia.client.particle.MagicParticle;
import com.minelittlepony.unicopia.client.particle.RaindropsParticle;
import com.minelittlepony.unicopia.client.particle.SphereParticle;
import com.minelittlepony.unicopia.client.render.ButterflyEntityRenderer;
import com.minelittlepony.unicopia.client.render.CloudEntityRenderer;
import com.minelittlepony.unicopia.client.render.CuccoonEntityRenderer;
import com.minelittlepony.unicopia.client.render.RainbowEntityRenderer;
import com.minelittlepony.unicopia.client.render.SpearEntityRenderer;
import com.minelittlepony.unicopia.client.render.SpellbookEntityRender;
import com.minelittlepony.unicopia.client.render.SpellcastEntityRenderer;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.particles.UParticles;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public interface URenderers {
    static void bootstrap() {
        EntityRendererRegistry.INSTANCE.register(UEntities.CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.WILD_CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.CONSTRUCTION_CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.RACING_CLOUD, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.MAGIC_SPELL, SpellcastEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.THROWN_ITEM, (manager, context) -> new FlyingItemEntityRenderer<>(manager, context.getItemRenderer()));
        EntityRendererRegistry.INSTANCE.register(UEntities.SPELLBOOK, SpellbookEntityRender::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.RAINBOW, RainbowEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.BUTTERFLY, ButterflyEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.CUCCOON, CuccoonEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(UEntities.THROWN_SPEAR, SpearEntityRenderer::new);

        ParticleFactoryRegistry.getInstance().register(UParticles.UNICORN_MAGIC, MagicParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.CHANGELING_MAGIC, ChangelingMagicParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.RAIN_DROPS, RaindropsParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.SPHERE, SphereParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(UParticles.DISK, DiskParticle.Factory::new);
    }
}
