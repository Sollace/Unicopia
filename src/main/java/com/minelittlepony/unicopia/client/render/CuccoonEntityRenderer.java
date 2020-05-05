package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.render.model.CuccoonEntityModel;
import com.minelittlepony.unicopia.entity.CuccoonEntity;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class CuccoonEntityRenderer extends LivingEntityRenderer<CuccoonEntity, CuccoonEntityModel> {

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/entity/cuccoon.png");

    public CuccoonEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new CuccoonEntityModel(), 1);
    }

    @Override
    public Identifier getTexture(CuccoonEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(CuccoonEntity entity) {
        return 0;
    }

    @Override
    protected boolean hasLabel(CuccoonEntity mobEntity) {
        return super.hasLabel(mobEntity) && (mobEntity.shouldRenderName() || mobEntity.hasCustomName() && mobEntity == renderManager.targetedEntity);
    }
}
