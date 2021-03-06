package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.FloatingArtefactEntity;
import com.minelittlepony.unicopia.item.UItems;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class FloatingArtefactEntityRenderer extends EntityRenderer<FloatingArtefactEntity> {

    private final ItemRenderer itemRenderer;

    public FloatingArtefactEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager);
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FloatingArtefactEntity entity, float yaw, float timeDelta, MatrixStack transforms, VertexConsumerProvider renderContext, int lightUv) {

        ItemStack stack = entity.getStack();

        if (stack.isEmpty()) {
            stack = UItems.EMPTY_JAR.getDefaultStack();
        }

        final BakedModel model = this.itemRenderer.getHeldItemModel(stack, entity.world, null);

        final float variance = 0.25F;
        final float verticalOffset = entity.getVerticalOffset(timeDelta);
        final float modelScaleY = model.getTransformation().getTransformation(ModelTransformation.Mode.GROUND).scale.getY();

        float scale = 1.6F;

        transforms.push();
        transforms.scale(scale, scale, scale);
        transforms.translate(0, verticalOffset + variance * modelScaleY, 0);
        transforms.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(entity.getRotation(timeDelta)));

        itemRenderer.renderItem(stack, ModelTransformation.Mode.GROUND, false, transforms, renderContext, lightUv, OverlayTexture.DEFAULT_UV, model);

        transforms.pop();

        super.render(entity, yaw, timeDelta, transforms, renderContext, lightUv);
    }

    @Override
    public Identifier getTexture(FloatingArtefactEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }

}
