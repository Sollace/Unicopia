package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.render.entity.feature.EnergySwirlOverlayFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.FriendlyCreeperEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.CreeperEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class FriendlyCreeperEntityRenderer extends MobEntityRenderer<FriendlyCreeperEntity, CreeperEntityRenderState, FriendlyCreeperEntityRenderer.Model> {
    private static final Identifier FRIENDLY_TEXTURE = Unicopia.id("textures/entity/creeper/friendly.png");
    private static final Identifier UNFIRENDLY_TEXTURE = Identifier.ofVanilla("textures/entity/creeper/creeper.png");

    public FriendlyCreeperEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new Model(context.getPart(EntityModelLayers.CREEPER)), 0.5f);
        addFeature(new ChargeFeature(this, context.getEntityModels()));
    }

    @Override
    public CreeperEntityRenderState createRenderState() {
        return new CreeperEntityRenderState();
    }

    @Override
    public void updateRenderState(FriendlyCreeperEntity entity, CreeperEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.fuseTime = entity.getClientFuseTime(tickDelta);
        state.charged = entity.isCharged();
        state.shaking = entity.isConverting();
    }

    @Override
    protected void scale(CreeperEntityRenderState state, MatrixStack matrixStack) {
        float g = state.fuseTime;
        float h = 1.0f + MathHelper.sin(g * 100.0f) * g * 0.01f;
        g = MathHelper.clamp(g, 0.0f, 1.0f);
        g *= g;
        g *= g;
        float i = (1.0f + g * 0.4f) * h;
        float j = (1.0f + g * 0.1f) / h;
        matrixStack.scale(i, j, i);
    }

    @Override
    protected void setupTransforms(CreeperEntityRenderState state, MatrixStack matrices, float animationProgress, float scale) {
        super.setupTransforms(state, matrices, animationProgress, scale);
        if (state.isInPose(EntityPose.SITTING)) {
            matrices.translate(0, -0.25, 0);
        }
    }

    @Override
    protected float getAnimationCounter(CreeperEntityRenderState state) {
        float f = state.fuseTime;
        return (int)(f * 10) % 2 == 0 ? 0 : MathHelper.clamp(f, 0.5F, 1);
    }

    @Override
    public Identifier getTexture(CreeperEntityRenderState state) {
        return state.shaking ? UNFIRENDLY_TEXTURE : FRIENDLY_TEXTURE;
    }

    public static class Model extends CreeperEntityModel {
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
        public void setAngles(CreeperEntityRenderState state) {
            super.setAngles(state);
            if (state.isInPose(EntityPose.SITTING)) {
                float legSpread = 0.001F;
                leftHindLeg.originZ -= 3;
                leftHindLeg.pitch = MathHelper.HALF_PI;
                leftHindLeg.yaw = legSpread;
                rightHindLeg.originZ -= 3;
                rightHindLeg.pitch = MathHelper.HALF_PI;
                rightHindLeg.yaw = -legSpread;
                leftFrontLeg.originZ += 3;
                leftFrontLeg.pitch = -MathHelper.HALF_PI;
                leftFrontLeg.yaw = -legSpread;
                rightFrontLeg.originZ += 3;
                rightFrontLeg.pitch = -MathHelper.HALF_PI;
                rightFrontLeg.yaw = legSpread;
            }
        }
    }

    public static class ChargeFeature extends EnergySwirlOverlayFeatureRenderer<CreeperEntityRenderState, Model> {
        private static final Identifier SKIN = Identifier.ofVanilla("textures/entity/creeper/creeper_armor.png");
        private final Model model;

        public ChargeFeature(FeatureRendererContext<CreeperEntityRenderState, Model> context, LoadedEntityModels models) {
            super(context);
            model = new Model(models.getModelPart(EntityModelLayers.CREEPER_ARMOR));
        }

        @Override
        protected boolean shouldRender(CreeperEntityRenderState creeperEntityRenderState) {
            return creeperEntityRenderState.charged;
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
        protected Model getEnergySwirlModel() {
            return this.model;
        }
    }
}
