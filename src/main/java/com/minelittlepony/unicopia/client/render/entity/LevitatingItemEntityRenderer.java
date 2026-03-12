package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class LevitatingItemEntityRenderer extends EntityRenderer<LevitatingItemEntity, LevitatingItemEntityRenderer.State> {

    private final ItemRenderer itemRenderer;

    public LevitatingItemEntityRenderer(Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(LevitatingItemEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.yaw = entity.getYaw(tickDelta);
        state.miningPos = entity.getMiningPos().orElse(null);
        state.miningFace = entity.getMiningFace();
        state.stack = entity.getStack();
        state.stackModel = itemRenderer.getModel(state.stack, entity.getMaster(), ModelTransformationMode.GROUND);
        state.yOffset = MathHelper.sin(state.age / 20F) * 0.02F;
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        super.render(state, matrices, vertices, light);

        if (!state.stack.isEmpty()) {
            float scale = 1.4F;
            matrices.push();
            matrices.translate(0, state.yOffset, 0);
            matrices.scale(scale, scale, scale);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.yaw));

            if (state.miningPos != null) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90 * MathHelper.sin(state.age)));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10 * MathHelper.sin(state.age)));
            }

            itemRenderer.renderItem(state.stack, ModelTransformationMode.GROUND, false, matrices, vertices, light, OverlayTexture.DEFAULT_UV, state.stackModel);

            /*matrices.scale(1.2F, 1.2F, 1.2F);

            itemRenderer.renderItem(state.stack, ModelTransformationMode.GROUND, false, matrices, layer -> {
                return vertices.getBuffer(RenderLayerUtil.getTexture(layer)
                        .map(texture -> RenderLayers.getMagicColored(texture, RenderLayers.DEFAULT_MAGIC_COLOR))
                        .orElse(RenderLayers.getMagicColored()));
            }, light, OverlayTexture.DEFAULT_UV, state.stackModel);*/

            matrices.pop();


            if (state.miningPos != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (System.currentTimeMillis() % 5 == 0 && !client.isPaused()) {
                    client.particleManager.addBlockBreakingParticles(state.miningPos, state.miningFace);
                }
            }
        }
    }

    public static class State extends EntityRenderState {
        @Nullable
        public BlockPos miningPos;
        public Direction miningFace = Direction.UP;
        public ItemStack stack = ItemStack.EMPTY;
        @Nullable
        public BakedModel stackModel;
        public float yaw;
        public float yOffset;
    }
}
