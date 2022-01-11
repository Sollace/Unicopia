package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.particle.SphereModel;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.entity.FairyEntity;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class FairyEntityRenderer extends MobEntityRenderer<FairyEntity, FairyEntityRenderer.Model> {
    public FairyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new Model(), 0.25F);
        shadowOpacity = 0;
    }

    @Override
    public Identifier getTexture(FairyEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    public static class Model extends EntityModel<FairyEntity> {
        private float radius;

        public Model() {
            super(texture -> RenderLayers.getMagicColored());
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
            float thickness = 0.05F;

            matrices.push();
            matrices.translate(0, 1.5, 0);

            SphereModel.SPHERE.render(matrices, vertexConsumer, light, overlay, radius - thickness, red, green, blue, 0.5F);
            SphereModel.SPHERE.render(matrices, vertexConsumer, light, overlay, radius, red, green, blue, 0.3F);

            matrices.pop();
        }

        @Override
        public void setAngles(FairyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
            radius = 0.125F + (float)Math.sin(entity.age * 0.1F) / 100F;
        }
    }
}
