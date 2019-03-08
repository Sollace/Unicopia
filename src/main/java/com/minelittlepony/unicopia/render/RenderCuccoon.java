package com.minelittlepony.unicopia.render;

import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.EntityCuccoon;
import com.minelittlepony.unicopia.model.ModelCuccoon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderCuccoon extends RenderLivingBase<EntityCuccoon> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Unicopia.MODID, "textures/entity/cuccoon.png");

    public RenderCuccoon(RenderManager manager) {
        super(manager, new ModelCuccoon(), 1);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCuccoon entity) {
        return TEXTURE;
    }

    @Override
    protected float getDeathMaxRotation(EntityCuccoon entity) {
        return 0;
    }

    @Override
    public void doRender(EntityCuccoon entity, double x, double y, double z, float entityYaw, float partialTicks) {

        if (entity.isBeingRidden()) {
            Entity rider = entity.getPassengers().get(0);

            if (!(rider == Minecraft.getMinecraft().player) || UClient.instance().getViewMode() != 0) {
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();

                renderManager.renderEntity(rider, x, y + rider.getYOffset(), z, entityYaw, partialTicks, true);

                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
            }


        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
