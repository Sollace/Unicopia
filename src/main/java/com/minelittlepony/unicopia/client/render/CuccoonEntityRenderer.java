package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.UnicopiaCore;
import com.minelittlepony.unicopia.client.render.model.CuccoonEntityModel;
import com.minelittlepony.unicopia.entity.CuccoonEntity;
import com.mojang.blaze3d.platform.GlStateManager;

import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class CuccoonEntityRenderer extends LivingEntityRenderer<CuccoonEntity, CuccoonEntityModel> {

    private static final Identifier TEXTURE = new Identifier(UnicopiaCore.MODID, "textures/entity/cuccoon.png");

    public CuccoonEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new CuccoonEntityModel(), 1);
    }

    @Override
    protected Identifier getTexture(CuccoonEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(CuccoonEntity entity) {
        return 0;
    }

    @Override
    public void render(CuccoonEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {

        if (entity.hasPassengers()) {
            Entity rider = entity.getPrimaryPassenger();

            if (!(rider == MinecraftClient.getInstance().player) || InteractionManager.instance().getViewMode() != 0) {
                GlStateManager.enableAlphaTest();
                GlStateManager.enableBlend();

                renderManager.render(rider, x, y + rider.getMountedHeightOffset(), z, entityYaw, partialTicks, true);

                GlStateManager.disableBlend();
                GlStateManager.disableAlphaTest();
            }
        }

        super.render(entity, x, y, z, entityYaw, partialTicks);
    }
}
