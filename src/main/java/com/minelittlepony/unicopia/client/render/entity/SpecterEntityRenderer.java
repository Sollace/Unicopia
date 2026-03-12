package com.minelittlepony.unicopia.client.render.entity;

import java.util.Set;

import org.joml.Quaternionf;

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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;

public class SpecterEntityRenderer extends BipedEntityRenderer<SpecterEntity, SpecterEntityRenderer.State, SpecterEntityRenderer.SpecterEntityModel> implements HitboxController<SpecterEntity> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/specter.png");

    public SpecterEntityRenderer(Context context) {
        super(context, new SpecterEntityModel(context.getPart(EntityModelLayers.PLAYER)), 0);
        addFeature(new HeadFeature(this));
        addFeature(new ArmorFeatureRenderer<>(this,
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                context.getEquipmentRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(SpecterEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        state.inverseCameraAngle = camera.getRotation();
        state.cameraDistance = entity.squaredDistanceTo(camera.getPos());
        state.alpha = state.cameraDistance <= 400 ? 0 : ColorHelper.channelFromFloat((float)Math.clamp(((state.cameraDistance - 400D) / 600D), 0, 1));

        long now = System.currentTimeMillis();
        if (entity.lastHitboxRenderTime > now - 3000) {
            state.alpha = 0;
        }
        if (entity.lastInViewportTime > now - 100) {
            if (entity.hideInViewportTime < now) {
                state.alpha = 0;
            }
        } else {
            entity.hideInViewportTime = now + 300 + entity.getId();
        }
        entity.lastInViewportTime = now;
    }

    @Override
    public Identifier getTexture(State state) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRenderHitbox(SpecterEntity entity) {
        entity.lastHitboxRenderTime = System.currentTimeMillis();
        return false;
    }

    @Override
    protected void setupTransforms(State state, MatrixStack matrices, float animationProgress, float bodyYaw) {
        super.setupTransforms(state, matrices, animationProgress, 0);
        matrices.multiply(state.inverseCameraAngle);
    }

    static class State extends BipedEntityRenderState {
        public Quaternionf inverseCameraAngle = new Quaternionf();
        public double cameraDistance;
        public int alpha;
    }

    static class HeadFeature extends FeatureRenderer<State, SpecterEntityModel> {
        private final SpecterEyesModel model = new SpecterEyesModel(SpecterEyesModel.createModelData().createModel());

        public HeadFeature(FeatureRendererContext<State, SpecterEntityModel> context) {
            super(context);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, State state, float limbAngle, float limbDistance) {
            if (!state.invisible && state.alpha > 0) {
                model.setAngles(state);
                FeatureRenderer.renderModel(model, TEXTURE, matrices, vertices, light, state, ColorHelper.withAlpha(state.alpha, Colors.WHITE));
            }
        }

    }

    static class SpecterEntityModel extends BipedEntityModel<State> {
        public SpecterEntityModel(ModelPart root) {
            super(root, RenderLayers::getEyes);
            root.hidden = true;
        }
    }

    static class SpecterEyesModel extends EntityModel<State> {
        public SpecterEyesModel(ModelPart root) {
            super(root, RenderLayers::getEyes);
        }

        static TexturedModelData createModelData() {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();
            root.addChild("eyes", ModelPartBuilder.create().uv(0, 0).cuboid(-4, -6, -4, 8, 8, 8, Set.of(Direction.NORTH)), ModelTransform.NONE);
            return TexturedModelData.of(data, 64, 32);
        }

        @Override
        public void setAngles(State state) {
            super.setAngles(state);
            float scale = 1 + state.alpha / 80F;
            getRootPart().visible = state.alpha > 0;
            getRootPart().xScale = scale;
            getRootPart().yScale = scale;
            getRootPart().zScale = scale;
        }
    }
}
