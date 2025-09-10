package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.MimicEntity;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;

import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class MimicEntityRenderer extends MobEntityRenderer<MimicEntity, MimicEntityRenderer.State, MimicEntityRenderer.MimicModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/chest/normal.png");

    public MimicEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new MimicModel(MimicModel.getTexturedModelData().createModel()), 0);
        addFeature(new ChestFeature(this));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(MimicEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.peekAmount = entity.getPeekAmount();
        state.legAngle = entity.limbAnimator.getPos(tickDelta);
        state.legSpeed = entity.limbAnimator.getSpeed(tickDelta);
        state.bodyTilt = MathHelper.cos(state.legAngle * 0.6662F) * 1.4F * state.legSpeed * 10 * state.peekAmount;
        state.destructionStage = FloatingArtefactEntityRenderer.getDestructionStage(entity);
        state.tickDelta = tickDelta;

        state.tileData = entity.getChestData();
        if (state.tileData != null) {
            state.tileData.setWorld(entity.getWorld());
            ((MixinBlockEntity)state.tileData).setPos(entity.getBlockPos());

            var properties = CloudChestBlockEntityRenderer.getProperties(state.tileData.getCachedState(), state.tileData);
            float progress = 1 - (float)Math.pow(1 - properties.apply(ChestBlock.getAnimationProgressRetriever(state.tileData)).get(tickDelta), 3);
            state.mouthOpenAmount = -(progress * 1.5707964f);
        } else {
            state.mouthOpenAmount = 0;
        }
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.translate(0, 0.3F * state.peekAmount, 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.bodyTilt));
        super.render(state, matrices, FloatingArtefactEntityRenderer.getDestructionOverlayProvider(matrices, vertices, 1, state.destructionStage), light);
        matrices.pop();
    }

    @Override
    public Identifier getTexture(State state) {
        return TEXTURE;
    }

    @Override
    protected float method_3919() {
        return 0;
    }

    @Override
    protected boolean canBeCulled(MimicEntity entity) {
        return true;
    }

    public static class State extends LivingEntityRenderState {
        public float peekAmount;
        public float legAngle;
        public float legSpeed;
        public float bodyTilt;
        public int destructionStage;
        public float mouthOpenAmount;
        @Nullable
        public ChestBlockEntity tileData;
        public float tickDelta;
    }

    static class ChestFeature extends FeatureRenderer<State, MimicModel> {
        public ChestFeature(FeatureRendererContext<State, MimicModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, State entity, float limbAngle, float limbDistance) {
            if (entity.tileData != null) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-entity.pitch));
                matrices.push();
                matrices.translate(-0.5, -1.5, -0.5);
                MinecraftClient.getInstance().getBlockEntityRenderDispatcher().render(entity.tileData, entity.tickDelta, matrices, vertexConsumers);
                matrices.pop();
            }
        }
    }

    static class MimicModel extends EntityModel<MimicEntityRenderer.State> {
        private ModelPart lid;
        private ModelPart leftLeg;
        private ModelPart rightLeg;

        public MimicModel(ModelPart part) {
            super(part);
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
                    //.uv(0, 0).cuboid(-7, 0, 0, 14, 5, 14, Dilation.NONE) //lid
                    //.uv(0, 0).cuboid(-1, -2.0F, 14.0F, 2.0F, 4.0F, 1.0F) //lock
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
            //root.addChild("base", ModelPartBuilder.create().uv(0, 19).cuboid(1, 0, 1, 14, 10, 14), ModelTransform.of(8, 24, -8, 0, 0, -3.1416F));
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
        public void setAngles(State entity) {
            getRootPart().yaw = MathHelper.RADIANS_PER_DEGREE * 180;
            getRootPart().pitch = -entity.pitch * MathHelper.RADIANS_PER_DEGREE;
            lid.pitch = entity.mouthOpenAmount;
            rightLeg.resetTransform();
            leftLeg.resetTransform();
            rightLeg.pitch = MathHelper.cos(entity.limbFrequency * 0.6662F) * 1.4F * entity.limbAmplitudeMultiplier;
            leftLeg.pitch = MathHelper.cos(entity.limbFrequency * 0.6662F + (float) Math.PI) * 1.4F * entity.limbAmplitudeMultiplier;
            float revealPercentage = entity.peekAmount;
            float velocy = (1 - revealPercentage) * -10F;
            rightLeg.pivotY += velocy;
            leftLeg.pivotY += velocy;
            rightLeg.visible = revealPercentage > 0.2F;
            leftLeg.visible = revealPercentage > 0.2F;
        }
    }
}