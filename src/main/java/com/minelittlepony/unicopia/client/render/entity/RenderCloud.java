package com.minelittlepony.unicopia.client.render.entity;

import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.client.render.entity.model.ModelCloud;
import com.minelittlepony.unicopia.entity.CloudEntity;
import com.minelittlepony.util.WorldHelper;

public class RenderCloud extends LivingEntityRenderer<CloudEntity, ModelCloud> {
    private static final Identifier cloud = new Identifier("unicopia", "textures/entity/clouds.png");
    private static final Identifier rainCloud = new Identifier("unicopia", "textures/entity/clouds_storm.png");

    public RenderCloud(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new ModelCloud(), 1f);
    }

    @Override
    public float prepareScale(CloudEntity entity, float par2) {
        float scale = entity.getCloudSize();

        GL11.glScalef(scale, scale, scale);
        return 0.0625F;
    }

    @Override
    protected void renderModel(CloudEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

        if (!entity.isDead) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, -entity.height/entity.getCloudSize() + 0.3F, 0);

            GlStateManager.disableLighting();
            GlStateManager.enableBlend();

            float brightness = Math.max(WorldHelper.getDaylightBrightness(entity.getEntityWorld(), 0) * 3, 0.05F);

            GlStateManager.color(brightness, brightness, brightness, entity.getOpaque() ? 1 : 0.8F);

            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.color(1, 1, 1, 1);

            GlStateManager.popMatrix();
        }
    }

    @Override
    protected Identifier getEntityTexture(CloudEntity entity) {
        if (entity.getIsRaining() && entity.getIsThundering()) {
            return rainCloud;
        }
        return cloud;
    }

    @Override
    protected int getColorMultiplier(CloudEntity par1LivingEntity, float yaw, float pitch) {
        return 0;
    }

    @Override
    protected float getDeathMaxRotation(CloudEntity par1LivingEntity) {
        return 0;
    }
}










