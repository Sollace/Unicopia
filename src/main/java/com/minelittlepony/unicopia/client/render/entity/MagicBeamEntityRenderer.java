package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.projectile.MagicBeamEntity;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MagicBeamEntityRenderer extends EntityRenderer<MagicBeamEntity, MagicBeamEntityRenderer.State> {
    private final Model model;

    public MagicBeamEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new Model(Model.getData().createModel());
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(MagicBeamEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.scale = 1 - tickDelta/30F;
        state.yaw = entity.getYaw(tickDelta) * MathHelper.RADIANS_PER_DEGREE;
        state.pitch = -entity.getPitch(tickDelta) * MathHelper.RADIANS_PER_DEGREE;
        state.color = entity.getSpellSlot().get().map(spell -> (0x99 << 24) | spell.getTypeAndTraits().type().getColor()).orElse(0);
    }

    @Override
    protected int getBlockLight(MagicBeamEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (state.age < 2 && dispatcher.camera.getFocusedEntity().squaredDistanceTo(state.x, state.y, state.z) < 8) {
            return;
        }

        matrices.push();
        matrices.scale(state.scale, state.scale, state.scale);

        model.setAngles(state);
        model.render(matrices, vertexConsumers.getBuffer(state.color == 0 ? RenderLayers.getMagicColored() : RenderLayers.getMagicColored(state.color)), light, OverlayTexture.DEFAULT_UV, Colors.WHITE);

        matrices.pop();
        super.render(state, matrices, vertexConsumers, light);
    }

    public static class State extends EntityRenderState {
        public float scale;
        public float yaw;
        public float pitch;

        public int color;
    }

    public class Model extends EntityModel<State> {

        public Model(ModelPart root) {
            super(root, texture -> RenderLayers.getMagicColored());
        }

        static TexturedModelData getData() {
            ModelData data = new ModelData();
            ModelPartData tree = data.getRoot();

            tree.addChild("beam", ModelPartBuilder.create()
                    .cuboid(0, 0, 0, 1, 1, 17)
                    .cuboid(0, 0, 0, 1, 1, 17, new Dilation(0.25F)), ModelTransform.NONE);

            return TexturedModelData.of(data, 64, 64);
        }

        @Override
        public void setAngles(State state) {
            super.setAngles(state);
            ModelPart part = getRootPart();
            part.pitch = state.pitch;
            part.yaw = state.yaw;
        }
    }
}
