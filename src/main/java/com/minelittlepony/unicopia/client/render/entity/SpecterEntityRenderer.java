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
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;

public class SpecterEntityRenderer extends BipedEntityRenderer<SpecterEntity, SpecterEntityRenderer.State, SpecterEntityRenderer.SpecterEntityModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/specter.png");

    public SpecterEntityRenderer(Context context) {
        super(context, new SpecterEntityModel(SpecterEntityModel.createModelData().createModel()), 0);
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
    }

    @Override
    public Identifier getTexture(State state) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(State state, MatrixStack matrices, float animationProgress, float bodyYaw) {
        super.setupTransforms(state, matrices, animationProgress, bodyYaw);
        matrices.multiply(state.inverseCameraAngle);
    }

    static class State extends BipedEntityRenderState {
        public Quaternionf inverseCameraAngle = new Quaternionf();
        public double cameraDistance;
        public int alpha;
    }

    static class SpecterEntityModel extends BipedEntityModel<State> {
        public SpecterEntityModel(ModelPart root) {
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
            getRootPart().visible = state.alpha <= 0;
            getRootPart().xScale = scale;
            getRootPart().yScale = scale;
            getRootPart().zScale = scale;
        }
    }
}
