package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.entity.model.ModelGem;
import com.minelittlepony.unicopia.entity.SpellcastEntity;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class RenderGem extends LivingEntityRenderer<SpellcastEntity, ModelGem> {

    private static final Identifier gem = new Identifier("unicopia", "textures/entity/gem.png");

    public RenderGem(EntityRenderDispatcher rendermanagerIn) {
        super(rendermanagerIn, new ModelGem(), 0);
    }

    @Override
    protected Identifier getTexture(SpellcastEntity entity) {
        return gem;
    }

    @Override
    public boolean shouldRender(SpellcastEntity livingEntity, ICamera camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    protected float getDeathMaxRotation(SpellcastEntity entity) {
        return 0;
    }

    @Override
    protected boolean canRenderName(SpellcastEntity targetEntity) {
        return super.canRenderName(targetEntity) && (targetEntity.getAlwaysRenderNameTagForRender()
                 || targetEntity.hasCustomName() && targetEntity == renderManager.pointedEntity);
    }
}
