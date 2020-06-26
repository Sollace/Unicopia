package com.minelittlepony.unicopia.world.client.render;

import com.minelittlepony.unicopia.world.client.render.model.CucoonEntityModel;
import com.minelittlepony.unicopia.world.entity.CucoonEntity;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class CucoonEntityRenderer extends LivingEntityRenderer<CucoonEntity, CucoonEntityModel> {

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/entity/cucoon.png");

    public CucoonEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new CucoonEntityModel(), 1);
    }

    @Override
    public Identifier getTexture(CucoonEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(CucoonEntity entity) {
        return 0;
    }

    @Override
    protected boolean hasLabel(CucoonEntity mobEntity) {
        return super.hasLabel(mobEntity) && (mobEntity.shouldRenderName() || mobEntity.hasCustomName() && mobEntity == dispatcher.targetedEntity);
    }
}
