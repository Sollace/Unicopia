package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.client.render.ButterflyEntityRenderer;
import com.minelittlepony.unicopia.client.render.CloudEntityRenderer;
import com.minelittlepony.unicopia.client.render.CuccoonEntityRenderer;
import com.minelittlepony.unicopia.client.render.RainbowEntityRenderer;
import com.minelittlepony.unicopia.client.render.SpearEntityRenderer;
import com.minelittlepony.unicopia.client.render.RenderSpellbook;
import com.minelittlepony.unicopia.client.render.SpellcastEntityRenderer;
import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.minelittlepony.unicopia.entity.CloudEntity;
import com.minelittlepony.unicopia.entity.CuccoonEntity;
import com.minelittlepony.unicopia.entity.ProjectileEntity;
import com.minelittlepony.unicopia.entity.RainbowEntity;
import com.minelittlepony.unicopia.entity.SpearEntity;
import com.minelittlepony.unicopia.entity.SpellbookEntity;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

public interface URenderers {
    static void bootstrap() {
        EntityRendererRegistry.INSTANCE.register(CloudEntity.class, CloudEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(SpellcastEntity.class, SpellcastEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(ProjectileEntity.class, (manager, context) -> new FlyingItemEntityRenderer<>(manager, context.getItemRenderer()));
        EntityRendererRegistry.INSTANCE.register(SpellbookEntity.class, RenderSpellbook::new);
        EntityRendererRegistry.INSTANCE.register(RainbowEntity.class, RainbowEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(ButterflyEntity.class, ButterflyEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(CuccoonEntity.class, CuccoonEntityRenderer::new);
        EntityRendererRegistry.INSTANCE.register(SpearEntity.class, SpearEntityRenderer::new);

        // TODO: ParticleFactoryRegistry
        //ParticleFactoryRegistry.instance().register(UParticles.UNICORN_MAGIC, MagicParticle::new);
        //ParticleFactoryRegistry.instance().register(UParticles.CHANGELING_MAGIC, ChangelingMagicParticle::new);
        //ParticleFactoryRegistry.instance().register(UParticles.RAIN_DROPS, RaindropsParticle::new);
        //ParticleFactoryRegistry.instance().register(UParticles.SPHERE, SphereParticle::new);
        //ParticleFactoryRegistry.instance().register(UParticles.DISK, DiskParticle::new);
    }
}
