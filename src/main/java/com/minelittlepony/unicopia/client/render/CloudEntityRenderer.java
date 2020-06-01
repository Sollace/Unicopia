package com.minelittlepony.unicopia.client.render;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import com.minelittlepony.unicopia.client.render.model.CloudEntityModel;
import com.minelittlepony.unicopia.entity.CloudEntity;

public class CloudEntityRenderer extends MobEntityRenderer<CloudEntity, CloudEntityModel> {
    private static final Identifier NORMAL = new Identifier("unicopia", "textures/entity/clouds.png");
    private static final Identifier RAINING = new Identifier("unicopia", "textures/entity/clouds_storm.png");

    public CloudEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new CloudEntityModel(), 1f);
    }

    @Override
    public void scale(CloudEntity entity, MatrixStack matrixStack, float par2) {
        float scale = entity.getCloudSize();

        matrixStack.scale(scale, scale, scale);
    }

    @Override
    public void render(CloudEntity entity, float f, float g, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int i) {

        if (entity.removed) {
            return;
        }

        matrices.push();
        matrices.translate(0, -entity.getHeight() + 0.3F, 0);

        //GlStateManager.disableLighting();
        //GlStateManager.enableBlend();

        //float brightness = Math.max(WorldHelper.getDaylightBrightness(entity.getEntityWorld(), 0) * 3, 0.05F);

        //GlStateManager.color4f(brightness, brightness, brightness, entity.getOpaque() ? 1 : 0.8F);

        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        super.render(entity, f, g, matrices, vertexConsumerProvider, i);

        //GlStateManager.disableBlend();
        //GlStateManager.enableLighting();
        //GlStateManager.color4f(1, 1, 1, 1);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(CloudEntity entity) {
        if (entity.getIsRaining() && entity.getIsThundering()) {
            return RAINING;
        }
        return NORMAL;
    }

    @Override
    protected float getLyingAngle(CloudEntity par1LivingEntity) {
        return 0;
    }
}










