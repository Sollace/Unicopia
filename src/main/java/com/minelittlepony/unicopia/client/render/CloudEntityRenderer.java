package com.minelittlepony.unicopia.client.render;

import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.client.render.model.CloudEntityModel;
import com.minelittlepony.unicopia.entity.CloudEntity;
import com.minelittlepony.unicopia.util.WorldHelper;
import com.mojang.blaze3d.platform.GlStateManager;

public class CloudEntityRenderer extends LivingEntityRenderer<CloudEntity, CloudEntityModel> {
    private static final Identifier cloud = new Identifier("unicopia", "textures/entity/clouds.png");
    private static final Identifier rainCloud = new Identifier("unicopia", "textures/entity/clouds_storm.png");

    public CloudEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new CloudEntityModel(), 1f);
    }

    @Override
    public void scale(CloudEntity entity, float par2) {
        float scale = entity.getCloudSize();

        GL11.glScalef(scale, scale, scale);
    }

    @Override
    protected void render(CloudEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

        if (!entity.removed) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(0, -entity.getHeight()/entity.getCloudSize() + 0.3F, 0);

            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            float brightness = Math.max(WorldHelper.getDaylightBrightness(entity.getEntityWorld(), 0) * 3, 0.05F);

            GlStateManager.color4f(brightness, brightness, brightness, entity.getOpaque() ? 1 : 0.8F);

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.color4f(1, 1, 1, 1);

            GlStateManager.popMatrix();
        }
    }

    @Override
    protected Identifier getTexture(CloudEntity entity) {
        if (entity.getIsRaining() && entity.getIsThundering()) {
            return rainCloud;
        }
        return cloud;
    }

    @Override
    protected float getLyingAngle(CloudEntity par1LivingEntity) {
        return 0;
    }
}










