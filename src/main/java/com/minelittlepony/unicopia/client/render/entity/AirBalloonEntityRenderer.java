package com.minelittlepony.unicopia.client.render.entity;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.collision.MultiBox;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity.BalloonDesign;
import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity.BasketType;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AirBalloonEntityRenderer extends MobEntityRenderer<AirBalloonEntity, AirBalloonEntityRenderer.State, AirBalloonEntityModel> {
    public AirBalloonEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new AirBalloonEntityModel(AirBalloonEntityModel.getBasketModelData().createModel()), 0);
        addFeature(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getBurnerModelData().createModel()), this,
                i -> i.hasBurner, e -> {
            return getComponentTexture(e.hasSoulFlame ? "soul_burner" : "burner");
        }, (light, entity) -> entity.isAscending ? 0xFF00FF : light));
        addFeature(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getCanopyModelData().createModel()), this,
                i -> i.hasBalloon,
                e -> getComponentTexture("canopy/" + e.design.asString()),
                (light, entity) -> entity.hasBurner && entity.isAscending ? light | 0x00005F : light)
        );
        addFeature(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getSandbagsModelData().createModel()),
                this, e -> e.hasBalloon && e.inflation >= 1, e -> getComponentTexture("sandbags"),
                (light, entity) -> entity.hasBurner && entity.isAscending ? light | 0x00003F : light));
    }

    @Override
    public void render(State entity, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        if (entity.hurt) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin(entity.age) * 3));
        }
        super.render(entity, matrices, vertices, light);
        matrices.pop();

        if (MinecraftClient.getInstance().getEntityRenderDispatcher().shouldRenderHitboxes() && !entity.invisible && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
            MultiBox.forEach(entity.boundingBox, box -> {
                VertexRendering.drawBox(matrices, vertices.getBuffer(RenderLayer.getLines()), box.offset(entity.pos.multiply(-1)), 1, 1, 1, 1);
            });
        }

    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(AirBalloonEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.yVelocity = MathHelper.clamp((float)Math.abs(entity.getVelocity().getY()) * 3F, 0.25F, 1F);
        state.yaw = entity.getHorizontalFacing().asRotation();
        state.design = entity.getDesign();
        state.basket = entity.getBasketType();
        state.inflation = entity.getInflation(tickDelta);
        state.hasBurner = entity.hasBurner();
        state.hasBalloon = entity.hasBalloon();
        state.isAscending = entity.isAscending();
        state.boundingBox = entity.getBoundingBox();
        state.hasSoulFlame = entity.getStackInHand(Hand.MAIN_HAND).isOf(Items.SOUL_LANTERN);
        state.pos = entity.getPos();
        state.burnerWiggleProgress = entity.getBurner().getPullProgress(tickDelta);
        state.basketRoll = MathHelper.clamp(entity.getXVelocity(tickDelta), -0.5F, 0.5F);
        state.basketPitch = MathHelper.clamp(entity.getZVelocity(tickDelta), -0.5F, 0.5F);
        state.accessoriesPivotX = state.inflation * MathHelper.sin(state.limbAmplitudeMultiplier + state.age / 5F) / 4F;
        state.burnerPivoyY = 32 * (1 - state.inflation) - (9 * state.inflation);
        state.sandbagsPitch = MathHelper.cos(state.limbAmplitudeMultiplier + state.age / 5F) / 80F;
        state.sandbagsRoll = MathHelper.sin(state.limbAmplitudeMultiplier + state.age / 5F) / 80F;
        if (state.leashData != null) {
            state.basketRoll *= -1;
            state.basketPitch *= -1;
        }
        for (int i = 0; i < state.sandbagPullAmounts.length; i++) {
            state.sandbagPullAmounts[i] = entity.getSandbag(i).getPullProgress(tickDelta);
        }

    }

    public static class State extends LivingEntityRenderState {
        public BalloonDesign design;
        public BasketType basket;

        public float yVelocity;

        public float inflation;
        public float burnerWiggleProgress;
        public float burnerPivoyY;

        public float basketPitch;
        public float basketRoll;

        public float accessoriesPivotX;

        public float sandbagsPitch;
        public float sandbagsRoll;

        public boolean hasBurner;
        public boolean hasBalloon;
        public boolean isAscending;
        public boolean hasSoulFlame;
        public Box boundingBox;
        public Vec3d pos;

        public float yaw;

        public float[] sandbagPullAmounts = new float[4];
    }

    @Override
    protected Box getBoundingBox(AirBalloonEntity entity) {
        if (entity.hasBalloon()) {
            return entity.getBalloonBoundingBox().withMinY(entity.getY());
        }
        return entity.getInteriorBoundingBox();
    }

    @Override
    public Identifier getTexture(State entity) {
        return getComponentTexture("basket/" + entity.basket.id().getPath());
    }

    @Override
    protected float method_3919() {
        return 90.0F;
    }

    private Identifier getComponentTexture(String componentName) {
        return Unicopia.id("textures/entity/air_balloon/" + componentName + ".png");
    }

    final class BalloonFeature extends FeatureRenderer<State, AirBalloonEntityModel> {
        private final AirBalloonEntityModel model;
        private final Predicate<State> visibilityTest;
        private final Function<State, Identifier> textureFunc;
        private final BiFunction<Integer, State, Integer> lightFunc;

        public BalloonFeature(AirBalloonEntityModel model,
                FeatureRendererContext<State, AirBalloonEntityModel> context,
                Predicate<State> visibilityTest,
                Function<State, Identifier> textureFunc,
                BiFunction<Integer, State, Integer> lightFunc) {
            super(context);
            this.model = model;
            this.visibilityTest = visibilityTest;
            this.textureFunc = textureFunc;
            this.lightFunc = lightFunc;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, State state, float limbDistance, float limbAngle) {
            if (visibilityTest.test(state)) {
                render(model, textureFunc.apply(state), matrices, vertices, lightFunc.apply(light, state), state, Colors.WHITE);
            }
        }
    }
}
