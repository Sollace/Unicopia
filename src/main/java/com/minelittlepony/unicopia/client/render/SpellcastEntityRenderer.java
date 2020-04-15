package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.client.render.model.GemEntityModel;
import com.minelittlepony.unicopia.entity.SpellcastEntity;

import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.render.VisibleRegion;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class SpellcastEntityRenderer extends LivingEntityRenderer<SpellcastEntity, GemEntityModel> {

    private static final Identifier gem = new Identifier("unicopia", "textures/entity/gem.png");

    public SpellcastEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new GemEntityModel(), 0);
    }

    @Override
    protected Identifier getTexture(SpellcastEntity entity) {
        return gem;
    }

    @Override
    public boolean isVisible(SpellcastEntity livingEntity, VisibleRegion camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    protected float getLyingAngle(SpellcastEntity entity) {
        return 0;
    }

    @Override
    protected boolean hasLabel(SpellcastEntity targetEntity) {
        return super.hasLabel(targetEntity) && (targetEntity.isCustomNameVisible()
                 || targetEntity.hasCustomName() && targetEntity == renderManager.targetedEntity);
    }
}
