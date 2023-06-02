package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.FloatingArtefactEntity;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class FloatingArtefactEntityRenderer extends EntityRenderer<FloatingArtefactEntity> {

    private final ItemRenderer itemRenderer;

    public FloatingArtefactEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(FloatingArtefactEntity entity, float yaw, float timeDelta, MatrixStack transforms, VertexConsumerProvider renderContext, int lightUv) {

        ItemStack stack = entity.getStack();

        if (stack.isEmpty()) {
            stack = UItems.EMPTY_JAR.getDefaultStack();
        }

        final BakedModel model = itemRenderer.getModel(stack, entity.world, null, 0);

        final float variance = 0.25F;
        final float verticalOffset = entity.getVerticalOffset(timeDelta);
        final float modelScaleY = model.getTransformation().getTransformation(ModelTransformationMode.GROUND).scale.y;

        float scale = 1.6F;

        transforms.push();
        transforms.scale(scale, scale, scale);
        transforms.translate(0, verticalOffset + variance * modelScaleY, 0);
        transforms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRotation(timeDelta)));

        itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, false, transforms, renderContext, lightUv, OverlayTexture.DEFAULT_UV, model);

        transforms.pop();

        super.render(entity, yaw, timeDelta, transforms, renderContext, lightUv);
    }

    @Override
    public Identifier getTexture(FloatingArtefactEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

}
