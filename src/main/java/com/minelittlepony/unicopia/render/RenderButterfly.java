package com.minelittlepony.unicopia.render;

import com.minelittlepony.unicopia.entity.EntityButterfly;
import com.minelittlepony.unicopia.model.ModelButterfly;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RenderButterfly extends RenderLiving<EntityButterfly> {

    public RenderButterfly(RenderManager rm) {
        super(rm, new ModelButterfly(), 0.25F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityButterfly entity) {
        return entity.getVariety().getSkin();
    }

    @Override
    protected void preRenderCallback(EntityButterfly entity, float ticks) {
        GlStateManager.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void applyRotations(EntityButterfly entity, float age, float yaw, float ticks) {

        if (!entity.isResting()) {
            GlStateManager.translate(0, MathHelper.cos(age / 3F) / 10F, 0);
        }

        super.applyRotations(entity, age, yaw, ticks);
    }
}
