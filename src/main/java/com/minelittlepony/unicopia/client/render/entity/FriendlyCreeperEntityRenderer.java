package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.render.entity.feature.EnergySwirlOverlayFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.FriendlyCreeperEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class FriendlyCreeperEntityRenderer extends MobEntityRenderer<FriendlyCreeperEntity, FriendlyCreeperEntityRenderer.Model> {
    private static final Identifier FRIENDLY_TEXTURE = Unicopia.id("textures/entity/creeper/friendly.png");
    private static final Identifier UNFIRENDLY_TEXTURE = Identifier.ofVanilla("textures/entity/creeper/creeper.png");

    public FriendlyCreeperEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new Model(context.getPart(EntityModelLayers.CREEPER)), 0.5f);
        addFeature(new ChargeFeature(this, context.getModelLoader()));
    }

    @Override
    protected void scale(FriendlyCreeperEntity creeperEntity, MatrixStack matrixStack, float f) {
        float g = creeperEntity.getClientFuseTime(f);
        float h = 1.0f + MathHelper.sin(g * 100.0f) * g * 0.01f;
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        g *= g;
        g *= g;
        float i = (1.0f + g * 0.4f) * h;
        float j = (1.0f + g * 0.1f) / h;
        matrixStack.scale(i, j, i);
    }

    @Override
    protected void setupTransforms(FriendlyCreeperEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale) {
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta, scale);
        if (entity.isSitting()) {
            matrices.translate(0, -0.25, 0);
        }
    }

    @Override
    protected boolean isShaking(FriendlyCreeperEntity entity) {
        return super.isShaking(entity) || entity.isConverting();
    }

    @Override
    protected float getAnimationCounter(FriendlyCreeperEntity entity, float f) {
        float fuseTime = entity.getClientFuseTime(f);
        if ((int)(fuseTime * 10) % 2 == 0) {
            return 0;
        }
        return MathHelper.clamp(fuseTime, 0.5f, 1.0f);
    }

    @Override
    public Identifier getTexture(FriendlyCreeperEntity entity) {
        return entity.isConverting() ? UNFIRENDLY_TEXTURE : FRIENDLY_TEXTURE;
    }

    public static class Model extends CreeperEntityModel<FriendlyCreeperEntity> {
        private final ModelPart leftHindLeg;
        private final ModelPart rightHindLeg;
        private final ModelPart leftFrontLeg;
        private final ModelPart rightFrontLeg;
        public Model(ModelPart root) {
            super(root);
            this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
            this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
            this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
            this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
        }

        @Override
        public void setAngles(FriendlyCreeperEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
            leftHindLeg.resetTransform();
            rightHindLeg.resetTransform();
            leftFrontLeg.resetTransform();
            rightFrontLeg.resetTransform();
            super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
            if (entity.isSitting()) {
                float legSpread = 0.001F;
                leftHindLeg.pivotZ -= 3;
                leftHindLeg.pitch = MathHelper.HALF_PI;
                leftHindLeg.yaw = legSpread;
                rightHindLeg.pivotZ -= 3;
                rightHindLeg.pitch = MathHelper.HALF_PI;
                rightHindLeg.yaw = -legSpread;
                leftFrontLeg.pivotZ += 3;
                leftFrontLeg.pitch = -MathHelper.HALF_PI;
                leftFrontLeg.yaw = -legSpread;
                rightFrontLeg.pivotZ += 3;
                rightFrontLeg.pitch = -MathHelper.HALF_PI;
                rightFrontLeg.yaw = legSpread;
            }
        }
    }

    public static class ChargeFeature extends EnergySwirlOverlayFeatureRenderer<FriendlyCreeperEntity, Model> {
        private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/creeper/creeper_armor.png");
        private final CreeperEntityModel<FriendlyCreeperEntity> model;

        public ChargeFeature(FeatureRendererContext<FriendlyCreeperEntity, Model> context, EntityModelLoader loader) {
            super(context);
            model = new Model(loader.getModelPart(EntityModelLayers.CREEPER_ARMOR));
        }

        @Override
        protected float getEnergySwirlX(float partialAge) {
            return partialAge * 0.01f;
        }

        @Override
        protected Identifier getEnergySwirlTexture() {
            return SKIN;
        }

        @Override
        protected EntityModel<FriendlyCreeperEntity> getEnergySwirlModel() {
            return this.model;
        }
    }
}
