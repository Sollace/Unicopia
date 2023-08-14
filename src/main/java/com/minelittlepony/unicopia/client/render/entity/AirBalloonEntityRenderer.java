package com.minelittlepony.unicopia.client.render.entity;

import java.util.function.Function;
import java.util.function.Predicate;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.AirBalloonEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public class AirBalloonEntityRenderer extends MobEntityRenderer<AirBalloonEntity, AirBalloonEntityModel> {
    public AirBalloonEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new AirBalloonEntityModel(AirBalloonEntityModel.getBasketModelData().createModel()), 0);
        addFeature(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getBurnerModelData().createModel()), this, AirBalloonEntity::hasBurner, e -> {
            return getComponentTexture(e.getStackInHand(Hand.MAIN_HAND).isOf(Items.SOUL_LANTERN) ? "soul_burner" : "burner");
        }));
        addFeature(new BalloonFeature(new AirBalloonEntityModel(AirBalloonEntityModel.getCanopyModelData().createModel()), this, AirBalloonEntity::hasBalloon, e -> getComponentTexture("canopy/" + e.getDesign().asString())));
    }

    @Override
    public void render(AirBalloonEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertices, light);

        if (MinecraftClient.getInstance().getEntityRenderDispatcher().shouldRenderHitboxes() && !entity.isInvisible() && !MinecraftClient.getInstance().hasReducedDebugInfo()) {
            for (Box box : entity.getBoundingBoxes()) {
                WorldRenderer.drawBox(matrices, vertices.getBuffer(RenderLayer.getLines()), box.offset(entity.getPos().multiply(-1)), 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public Identifier getTexture(AirBalloonEntity entity) {
        return getComponentTexture("basket/" + entity.getBasketType().id().getPath());
    }

    @Override
    protected float getLyingAngle(AirBalloonEntity entity) {
        return 0;
    }

    private Identifier getComponentTexture(String componentName) {
        return Unicopia.id("textures/entity/air_balloon/" + componentName + ".png");
    }

    final class BalloonFeature extends FeatureRenderer<AirBalloonEntity, AirBalloonEntityModel> {
        private final AirBalloonEntityModel model;
        private final Predicate<AirBalloonEntity> visibilityTest;
        private final Function<AirBalloonEntity, Identifier> textureFunc;

        public BalloonFeature(AirBalloonEntityModel model,
                FeatureRendererContext<AirBalloonEntity, AirBalloonEntityModel> context,
                Predicate<AirBalloonEntity> visibilityTest,
                Function<AirBalloonEntity, Identifier> textureFunc) {
            super(context);
            this.model = model;
            this.visibilityTest = visibilityTest;
            this.textureFunc = textureFunc;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, AirBalloonEntity entity,
                float limbAngle, float limbDistance, float tickDelta, float animationProgress, float yaw, float pitch) {
            if (visibilityTest.test(entity)) {
                render(getModel(), model, textureFunc.apply(entity), matrices, vertices, light, entity, limbAngle, limbDistance, 0, yaw, pitch, tickDelta, 1, 1, 1);
            }
        }
    }
}