package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.mob.FairyEntity;

import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.ColorHelper.Argb;

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
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
            float thickness = 0.05F;

            matrices.push();
            matrices.translate(0, 1.5, 0);


            SphereModel.SPHERE.render(matrices, vertexConsumer, light, overlay, radius - thickness, Argb.withAlpha(color, 127));
            SphereModel.SPHERE.render(matrices, vertexConsumer, light, overlay, radius, Argb.withAlpha(color, 85));

            matrices.pop();
        }

        @Override
        public void setAngles(FairyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
            radius = 0.125F + (float)Math.sin(entity.age * 0.1F) / 100F;
        }
    }
}
