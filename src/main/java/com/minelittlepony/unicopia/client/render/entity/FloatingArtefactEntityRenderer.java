package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.entity.mob.FloatingArtefactEntity;
import com.minelittlepony.unicopia.entity.mob.StationaryObjectEntity;
import com.minelittlepony.unicopia.item.UItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class FloatingArtefactEntityRenderer extends EntityRenderer<FloatingArtefactEntity, FloatingArtefactEntityRenderer.State> {

    private final ItemRenderer itemRenderer;

    public FloatingArtefactEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(FloatingArtefactEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        ItemStack stack = entity.getStack();

        if (stack.isEmpty()) {
            stack = UItems.EMPTY_JAR.getDefaultStack();
        }

        state.stack = stack;
        state.variance = 0.25F;
        state.scale = 1.6F;
        state.verticalOffset = entity.getVerticalOffset(tickDelta);
        state.itemModel = itemRenderer.getModel(stack, entity.getWorld(), null, 0);
        state.modelScaleY = state.itemModel.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y;
        state.yaw = entity.getRotation(tickDelta);
        state.destructionStage = getDestructionStage(entity);
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.scale(state.scale, state.scale, state.scale);
        matrices.translate(0, state.verticalOffset + state.variance * state.modelScaleY, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.yaw));
        itemRenderer.renderItem(state.stack, ModelTransformationMode.GROUND, false, matrices, getDestructionOverlayProvider(matrices, vertices, 4F, state.destructionStage), light, OverlayTexture.DEFAULT_UV, state.itemModel);
        matrices.pop();
        super.render(state, matrices, vertices, light);
    }

    public static class State extends EntityRenderState {
        public float variance = 0.25F;
        public float scale = 1.6F;
        public ItemStack stack;
        public BakedModel itemModel;
        public float verticalOffset;
        public float modelScaleY;
        public float yaw;

        public int destructionStage;
    }

    static int getDestructionStage(StationaryObjectEntity entity) {
        return getDestructionStage(entity.getHealth(), entity.getMaxHealth());
    }

    static int getDestructionStage(LivingEntity entity) {
        return getDestructionStage(entity.getHealth(), entity.getMaxHealth());
    }

    static int getDestructionStage(float health, float maxHealth) {
        return (int)(MathHelper.clamp(1F - (health / maxHealth), 0F, 1F) * (ModelBaker.field_32983 - 1F));
    }

    static VertexConsumerProvider getDestructionOverlayProvider(MatrixStack matrices, VertexConsumerProvider vertices, float scale, int stage) {
        if (stage <= 0) {
            return vertices;
        }
        final MatrixStack.Entry entry = matrices.peek();
        final OverlayVertexConsumer destructionOverlay = new OverlayVertexConsumer(
                MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers().getBuffer(RenderLayers.getCrumbling(MathHelper.clamp(stage, 0, ModelBaker.field_32983 - 1))),
                entry,
                scale
        );
        return layer -> layer.hasCrumbling() ? VertexConsumers.union(destructionOverlay, vertices.getBuffer(layer)) : vertices.getBuffer(layer);
    }
}
