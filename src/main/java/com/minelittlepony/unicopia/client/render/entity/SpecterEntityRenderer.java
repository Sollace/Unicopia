package com.minelittlepony.unicopia.client.render.entity;

import java.util.Set;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.entity.mob.SpecterEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;

public class SpecterEntityRenderer extends BipedEntityRenderer<SpecterEntity, SpecterEntityRenderer.SpecterEntityModel> implements HitboxController<SpecterEntity> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/specter.png");

    public SpecterEntityRenderer(Context context) {
        super(context, new SpecterEntityModel(context.getPart(EntityModelLayers.PLAYER)), 0);
        addFeature(new ArmorFeatureRenderer<>(this,
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public Identifier getTexture(SpecterEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRenderHitbox(SpecterEntity entity) {
        entity.lastHitboxRenderTime = System.currentTimeMillis();
        return false;
    }

    @Override
    protected void setupTransforms(SpecterEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale) {
        super.setupTransforms(entity, matrices, animationProgress, 0, tickDelta, scale);
        Camera cam = MinecraftClient.getInstance().gameRenderer.getCamera();
        matrices.multiply(cam.getRotation());
    }

    static class SpecterEntityModel extends BipedEntityModel<SpecterEntity> {
        private final ModelPart part;
        private int alpha;

        public SpecterEntityModel(ModelPart root) {
            super(root, RenderLayer::getEntityTranslucent);
            this.part = createModelData().createModel();
        }

        static TexturedModelData createModelData() {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();
            root.addChild("eyes", ModelPartBuilder.create().uv(0, 0).cuboid(-4, -6, -4, 8, 8, 8, Set.of(Direction.NORTH)), ModelTransform.NONE);
            return TexturedModelData.of(data, 64, 32);
        }

        @Override
        public void animateModel(SpecterEntity entity, float f, float g, float h) {
            super.animateModel(entity, f, g, h);
            double distance = entity.squaredDistanceTo(MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
            alpha = distance <= 400 ? 0 : ColorHelper.channelFromFloat((float)Math.clamp(((distance - 400D) / 600D), 0, 1));
            if (!entity.isAngryAt(MinecraftClient.getInstance().player)) {
                long now = System.currentTimeMillis();
                if (entity.lastHitboxRenderTime > now - 3000) {
                    alpha = 0;
                }
                if (entity.lastInViewportTime > now - 100) {
                    if (entity.hideInViewportTime < now) {
                        alpha = 0;
                    }
                } else {
                    entity.hideInViewportTime = now + 300 + entity.getId();
                }
                entity.lastInViewportTime = now;
            }
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
            if (alpha <= 0) {
                return;
            }
            vertices = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayers.getEyes(Unicopia.id("textures/entity/specter.png")));
            matrices.push();
            float scale = 1 + alpha / 80F;
            matrices.scale(scale, scale, scale);
            part.render(matrices, vertices, LightmapTextureManager.MAX_LIGHT_COORDINATE, overlay, ColorHelper.Argb.withAlpha(alpha, color));
            matrices.pop();
        }
    }
}
