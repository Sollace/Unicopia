package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.minelittlepony.unicopia.entity.mob.LevitatingItemEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class LevitatingItemEntityRenderer extends EntityRenderer<LevitatingItemEntity, LevitatingItemEntityRenderer.State> {

    private final ItemModelManager itemModelManager;

    public LevitatingItemEntityRenderer(Context ctx) {
        super(ctx);
        itemModelManager = ctx.getItemModelManager();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(LevitatingItemEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.miningPos = entity.getMiningPos().orElse(null);
        state.rotation.rotationY(entity.getYaw(tickDelta) * MathHelper.RADIANS_PER_DEGREE);
        if (state.miningPos != null) {
            state.rotation.rotateY(MathHelper.HALF_PI).rotateZ(MathHelper.HALF_PI * MathHelper.sin(state.age)).rotateX(MathHelper.HALF_PI / 9 * MathHelper.sin(state.age));
        }

        state.miningFace = entity.getMiningFace();
        state.stack = entity.getStack();
        state.yOffset = MathHelper.sin(state.age / 20F) * 0.02F;
        itemModelManager.clearAndUpdate(state.stackState, state.stack, ItemDisplayContext.GROUND, entity.getWorld(), null, 0);
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        super.render(state, matrices, vertices, light);

        if (!state.stack.isEmpty()) {
            float scale = 1.4F;
            matrices.push();
            matrices.translate(0, state.yOffset, 0);
            matrices.scale(scale, scale, scale);
            matrices.multiply(state.rotation);
            state.stackState.render(matrices, vertices, light, OverlayTexture.DEFAULT_UV);

            // TODO: Magic glow effect
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
        public final ItemRenderState stackState = new ItemRenderState();
        public final Quaternionf rotation = new Quaternionf();
        public float yOffset;
    }
}
