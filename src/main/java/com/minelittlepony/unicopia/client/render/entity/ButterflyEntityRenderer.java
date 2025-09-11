package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class ButterflyEntityRenderer extends MobEntityRenderer<ButterflyEntity, ButterflyEntityRenderer.State, ButterflyEntityRenderer.ButterflyEntityModel> {
    public ButterflyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ButterflyEntityModel(ButterflyEntityModel.getData().createModel()), 0.25F);
        shadowRadius = 0.2F;
        shadowOpacity = 0.3F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(ButterflyEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.resting = entity.isResting();
        state.variant = entity.getVariant();
        BlockPos pos = entity.getBlockPos();
        state.translucent = getBlockLight(entity, pos) < 7 && getSkyLight(entity, pos) < 15;
        state.bodyPitch = state.resting ? 0.8F : ((float)Math.PI / 4) + MathHelper.cos(state.age * 0.1F) * 0.15F;
        state.wingAngle = state.resting ? MathHelper.cos((state.age + (1 + entity.getId()) % 2) / 20) * (float)Math.PI / 6 + 0.7F : MathHelper.cos(state.age) * (float)Math.PI / 4;
    }

    @Override
    public Identifier getTexture(State state) {
        return state.variant.getSkin();
    }

    @Override
    protected void scale(State state, MatrixStack matrices) {
        matrices.scale(0.35F, 0.35F, 0.35F);
        matrices.translate(0.5F, 0, -0.5F);
    }

    @Override
    protected void setupTransforms(State state, MatrixStack matrices, float animationProgress, float bodyYaw) {
        if (!state.resting) {
            matrices.translate(0, MathHelper.cos(state.age / 3F) / 10F, 0);
        }
        super.setupTransforms(state, matrices, animationProgress, bodyYaw);
    }

    @Override
    @Nullable
    protected RenderLayer getRenderLayer(State state, boolean showBody, boolean translucent, boolean showOutline) {
        if (showBody && !translucent && state.translucent) {
            return RenderLayers.getEntityTranslucent(getTexture(state));
        }
        return super.getRenderLayer(state, showBody, translucent, showOutline);
    }

    @Override
    protected int getSkyLight(ButterflyEntity entity, BlockPos pos) {
        return (int)(super.getSkyLight(entity, pos) * (entity.getWorld() instanceof ClientWorld w ? w.getSkyBrightness(MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false)) : 1));
    }

    public static class State extends LivingEntityRenderState {
        public boolean resting;
        public ButterflyEntity.Variant variant = ButterflyEntity.Variant.BUTTERFLY;

        public boolean translucent;
        public float wingAngle;
        public float bodyPitch;
    }

    public static class ButterflyEntityModel extends EntityModel<State> {
        private final ModelPart leftWing;
        private final ModelPart rightWing;

        public ButterflyEntityModel(ModelPart tree) {
            super(tree, RenderLayers::getEntityAlpha);
            tree.pivotX = -10;
            tree.pivotY = 12;
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
        public void setAngles(State state) {
            getRootPart().pitch = state.bodyPitch;
            leftWing.yaw = -state.wingAngle;
            rightWing.yaw = state.wingAngle;
        }
    }
}
