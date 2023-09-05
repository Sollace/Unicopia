package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ButterflyEntityRenderer extends MobEntityRenderer<ButterflyEntity, ButterflyEntityRenderer.ButterflyEntityModel> {
    public ButterflyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ButterflyEntityModel(ButterflyEntityModel.getData().createModel()), 0.25F);
        shadowRadius = 0.2F;
        shadowOpacity = 0.3F;
    }

    @Override
    public Identifier getTexture(ButterflyEntity entity) {
        return entity.getVariant().getSkin();
    }

    @Override
    protected void scale(ButterflyEntity entity, MatrixStack matrices, float ticks) {
        matrices.scale(0.35F, 0.35F, 0.35F);
        matrices.translate(0.5F, 0, -0.5F);
    }

    @Override
    protected void setupTransforms(ButterflyEntity entity, MatrixStack matrices, float age, float yaw, float ticks) {

        if (!entity.isResting()) {
            matrices.translate(0, MathHelper.cos(age / 3F) / 10F, 0);
        }

        super.setupTransforms(entity, matrices, age, yaw, ticks);
    }

    public static class ButterflyEntityModel extends EntityModel<ButterflyEntity> {
        private final ModelPart body;
        private final ModelPart leftWing;
        private final ModelPart rightWing;

        public ButterflyEntityModel(ModelPart tree) {
            super(RenderLayers::getEntityAlpha);
            body = tree;
            body.pivotX = -10;
            body.pivotY = 12;
            leftWing = tree.getChild("left_wing");
            rightWing = tree.getChild("right_wing");
        }

        static TexturedModelData getData() {
            ModelData data = new ModelData();
            ModelPartData tree = data.getRoot();

            tree.addChild("right_wing", ModelPartBuilder.create().uv(42, 0).cuboid(-13, -5, 0, 10, 19, 1), ModelTransform.rotation(0, 0, -0.2F))
                .addChild("right_wing_outer", ModelPartBuilder.create().uv(24, 16).cuboid(0, 0, 0, 10, 12, 1), ModelTransform.of(-13, 10, 0.1F, 0, 0, -0.2F));

            tree.addChild("left_wing", ModelPartBuilder.create().uv(42, 0).mirrored().cuboid(2, -5, 0, 10, 19, 1), ModelTransform.rotation(0, 0, 0.2F))
                .addChild("left_wing_outer", ModelPartBuilder.create().uv(24, 16).cuboid(0, 0, 0, 10, 12, 1), ModelTransform.of(2, 10, 0.1F, 0, 0, 0.2F));

            return TexturedModelData.of(data, 64, 64);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
            body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }

        @Override
        public void setAngles(ButterflyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {

            float flap = MathHelper.cos(ageInTicks) * (float)Math.PI / 4;

            if (entity.isResting()) {
                body.pitch = 0.8F;
                flap = MathHelper.cos((ageInTicks + (1 + entity.getId()) % 2) / 20) * (float)Math.PI / 6 + 0.7F;
            } else {
                body.pitch = ((float)Math.PI / 4) + MathHelper.cos(ageInTicks * 0.1F) * 0.15F;
            }

            leftWing.yaw = -flap;
            rightWing.yaw = flap;
        }
    }
}
