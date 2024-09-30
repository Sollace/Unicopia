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
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class FloatingArtefactEntityRenderer extends EntityRenderer<FloatingArtefactEntity> {

    private final ItemRenderer itemRenderer;

    public FloatingArtefactEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(FloatingArtefactEntity entity, float yaw, float timeDelta, MatrixStack matrices, VertexConsumerProvider vertices, int lightUv) {

        ItemStack stack = entity.getStack();

        if (stack.isEmpty()) {
            stack = UItems.EMPTY_JAR.getDefaultStack();
        }

        final BakedModel model = itemRenderer.getModel(stack, entity.getWorld(), null, 0);

        final float variance = 0.25F;
        final float verticalOffset = entity.getVerticalOffset(timeDelta);
        final float modelScaleY = model.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y;

        float scale = 1.6F;

        matrices.push();
        matrices.scale(scale, scale, scale);
        matrices.translate(0, verticalOffset + variance * modelScaleY, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRotation(timeDelta)));

        itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false, matrices, getDestructionOverlayProvider(matrices, vertices, 4F, getDestructionStage(entity)), lightUv, OverlayTexture.DEFAULT_UV, model);

        matrices.pop();

        super.render(entity, yaw, timeDelta, matrices, vertices, lightUv);
    }

    @Override
    public Identifier getTexture(FloatingArtefactEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    static int getDestructionStage(StationaryObjectEntity entity) {
        return (int)(MathHelper.clamp(1F - (entity.getHealth() / entity.getMaxHealth()), 0F, 1F) * (ModelLoader.field_32983 - 1F));
    }

    static int getDestructionStage(LivingEntity entity) {
        return (int)(MathHelper.clamp(1F - (entity.getHealth() / entity.getMaxHealth()), 0F, 1F) * (ModelLoader.field_32983 - 1F));
    }

    static VertexConsumerProvider getDestructionOverlayProvider(MatrixStack matrices, VertexConsumerProvider vertices, float scale, int stage) {
        if (stage <= 0) {
            return vertices;
        }
        final MatrixStack.Entry entry = matrices.peek();
        final OverlayVertexConsumer destructionOverlay = new OverlayVertexConsumer(
                MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers().getBuffer(RenderLayers.getCrumbling(MathHelper.clamp(stage, 0, ModelLoader.field_32983 - 1))),
                entry,
                scale
        );
        return layer -> layer.hasCrumbling() ? VertexConsumers.union(destructionOverlay, vertices.getBuffer(layer)) : vertices.getBuffer(layer);
    }
}
