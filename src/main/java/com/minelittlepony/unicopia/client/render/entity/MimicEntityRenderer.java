package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.entity.mob.MimicEntity;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class MimicEntityRenderer extends MobEntityRenderer<MimicEntity, MimicEntityRenderer.MimicModel> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/chest/normal.png");

    public MimicEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new MimicModel(MimicModel.getTexturedModelData().createModel()), 0);
        addFeature(new ChestFeature(this));
    }

    @Override
    public void render(MimicEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.translate(0, 0.3F * entity.getPeekAmount(), 0);
        super.render(entity, yaw, tickDelta, matrices,
            FloatingArtefactEntityRenderer.getDestructionOverlayProvider(
                    matrices,
                    vertices,
                    1,
                    FloatingArtefactEntityRenderer.getDestructionStage(entity)
        ), light);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(MimicEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(MimicEntity entity) {
        return 0;
    }

    static class ChestFeature extends FeatureRenderer<MimicEntity, MimicModel> {
        public ChestFeature(FeatureRendererContext<MimicEntity, MimicModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, MimicEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
            BlockEntity tileData = entity.getChestData();
            if (tileData != null) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-entity.getPitch(tickDelta)));
                matrices.push();
                matrices.translate(-0.5, -1.5, -0.5);
                MinecraftClient.getInstance().getBlockEntityRenderDispatcher().render(tileData, tickDelta, matrices, vertexConsumers);
                matrices.pop();
            }
        }

    }

    static class MimicModel extends EntityModel<MimicEntity> {
        private ModelPart part;
        private ModelPart lid;
        private ModelPart leftLeg;
        private ModelPart rightLeg;

        public MimicModel(ModelPart part) {
            this.part = part;
            this.lid = part.getChild("lid");
            this.leftLeg = part.getChild("left_leg");
            this.rightLeg = part.getChild("right_leg");
        }

        public static TexturedModelData getTexturedModelData() {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();
            ModelPartData lid = root.addChild("lid", ModelPartBuilder.create(), ModelTransform.of(0, 15, -7, 0, 0, -3.1416F));
            lid.addChild("tongue", ModelPartBuilder.create()
                    .uv(11, 34).cuboid(-3, -11, 1, 6, 1, 8, Dilation.NONE), ModelTransform.of(0, 6, 9, 0.8F, 0, 0));
            lid.addChild("upper_teeth", ModelPartBuilder.create()
                    //.uv(0, 0).cuboid(-7, 0, 0, 14, 5, 14, Dilation.NONE)
                    .uv(0, 0).cuboid(-1, -2, 12, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(-4, -2, 12, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(2, -2, 12, 2, 4, 1, Dilation.NONE), ModelTransform.NONE)
                    .addChild("cube_r1", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-5, -2, -6, 2, 4, 1, Dilation.NONE)
                        .uv(0, 0).cuboid(-8, -2, -6, 2, 4, 1, Dilation.NONE)
                        .uv(0, 0).cuboid(-11, -2, -6, 2, 4, 1, Dilation.NONE)
                        .uv(0, 0).cuboid(-5, -2, 5, 2, 4, 1, Dilation.NONE)
                        .uv(0, 0).cuboid(-8, -2, 5, 2, 4, 1, Dilation.NONE)
                        .uv(0, 0).cuboid(-11, -2, 5, 2, 4, 1, Dilation.NONE), ModelTransform.of(0, 0, 0, 0, 1.5708F, 0));
            root.addChild("lower_teeth", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-1, -1, 12, 2, 4, 1, Dilation.NONE)
                .uv(0, 0).cuboid(-4, -1, 12, 2, 4, 1, Dilation.NONE)
                .uv(0, 0).cuboid(2, -1, 12, 2, 4, 1, Dilation.NONE), ModelTransform.pivot(0, 13, -7))
                .addChild("cube_r2", ModelPartBuilder.create()
                    .uv(0, 0).cuboid(-6, -1, -6, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(-9, -1, -6, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(-12, -1, -6, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(-6, -1, 5, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(-9, -1, 5, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(-12, -1, 5, 2, 4, 1, Dilation.NONE), ModelTransform.of(0, 0, 0, 0, 1.5708F, 0));
            root.addChild("right_leg", ModelPartBuilder.create()
                    .uv(7, 30).cuboid(-2.5F, -1.5F, -3.5F, 5, 7, 6, Dilation.NONE), ModelTransform.pivot(3.5F, 23.5F, 0));
            root.addChild("left_leg", ModelPartBuilder.create()
                    .uv(7, 30).mirrored().cuboid(-9.5F, -1.5F, -3.5F, 5, 7, 6, Dilation.NONE), ModelTransform.pivot(3.5F, 23.5F, 0));
            return TexturedModelData.of(data, 64, 64);
        }

        @Override
        public void animateModel(MimicEntity entity, float limbAngle, float limbDistance, float tickDelta) {
            ChestBlockEntity tileData = entity.getChestData();
            if (tileData != null) {
                var properties = CloudChestBlockEntityRenderer.getProperties(tileData.getCachedState(), tileData);
                float progress = 1 - (float)Math.pow(1 - properties.apply(ChestBlock.getAnimationProgressRetriever(tileData)).get(tickDelta), 3);
                lid.pitch = -(progress * 1.5707964f);
            } else {
                lid.pitch = 0;
            }
            part.yaw = MathHelper.RADIANS_PER_DEGREE * 180;
            part.pitch = -entity.getPitch(tickDelta) * MathHelper.RADIANS_PER_DEGREE;
        }

        @Override
        public void setAngles(MimicEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
            rightLeg.resetTransform();
            leftLeg.resetTransform();
            rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
            leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.4F * limbDistance;
            float revealPercentage = entity.getPeekAmount();
            float velocy = (1 - revealPercentage) * -10F;
            rightLeg.pivotY += velocy;
            leftLeg.pivotY += velocy;
            rightLeg.visible = revealPercentage > 0.2F;
            leftLeg.visible = revealPercentage > 0.2F;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
            part.render(matrices, vertices, light, overlay);
        }
    }
}