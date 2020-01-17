package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.entity.model.ButterflyEntityModel;
import com.minelittlepony.unicopia.entity.ButterflyEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ButterflyEntityRenderer extends LivingEntityRenderer<ButterflyEntity, ButterflyEntityModel> {

    public ButterflyEntityRenderer(EntityRenderDispatcher rm) {
        super(rm, new ButterflyEntityModel(), 0.25F);
    }

    @Override
    protected Identifier getTexture(ButterflyEntity entity) {
        return entity.getVariety().getSkin();
    }

    @Override
    protected void scale(ButterflyEntity entity, float ticks) {
        GlStateManager.scalef(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void setupTransforms(ButterflyEntity entity, float age, float yaw, float ticks) {

        if (!entity.isResting()) {
            GlStateManager.translated(0, MathHelper.cos(age / 3F) / 10F, 0);
        }

        super.setupTransforms(entity, age, yaw, ticks);
    }
}
