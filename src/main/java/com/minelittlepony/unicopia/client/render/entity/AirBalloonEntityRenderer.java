package com.minelittlepony.unicopia.client.render.entity;

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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;

public class AirBalloonEntityRenderer extends MobEntityRenderer<AirBalloonEntity, AirBalloonEntityModel> {
    public AirBalloonEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new AirBalloonEntityModel(AirBalloonEntityModel.getBasketModelData().createModel()), 0);
        addFeature(new BalloonFeature("burner", new AirBalloonEntityModel(AirBalloonEntityModel.getBurnerModelData().createModel()), this, AirBalloonEntity::hasBurner));
        addFeature(new BalloonFeature("canopy", new AirBalloonEntityModel(AirBalloonEntityModel.getCanopyModelData().createModel()), this, AirBalloonEntity::hasBalloon));
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
        return getComponentTexture(entity, "basket");
    }

    @Override
    protected float getLyingAngle(AirBalloonEntity entity) {
        return 0;
    }

    private Identifier getComponentTexture(AirBalloonEntity entity, String componentName) {
        return Unicopia.id("textures/entity/air_balloon/" + componentName + ".png");
    }

    final class BalloonFeature extends FeatureRenderer<AirBalloonEntity, AirBalloonEntityModel> {
        private final AirBalloonEntityModel model;
        private final Predicate<AirBalloonEntity> visibilityTest;
        private final String componentName;

        public BalloonFeature(String componentName, AirBalloonEntityModel model,
                FeatureRendererContext<AirBalloonEntity, AirBalloonEntityModel> context,
                Predicate<AirBalloonEntity> visibilityTest) {
            super(context);
            this.componentName = componentName;
            this.model = model;
            this.visibilityTest = visibilityTest;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, AirBalloonEntity entity,
                float limbAngle, float limbDistance, float tickDelta, float animationProgress, float yaw, float pitch) {
            if (visibilityTest.test(entity)) {
                render(getModel(), model, getComponentTexture(entity, componentName), matrices, vertices, light, entity, limbAngle, limbDistance, 0, yaw, pitch, tickDelta, 1, 1, 1);
            }
        }
    }
}