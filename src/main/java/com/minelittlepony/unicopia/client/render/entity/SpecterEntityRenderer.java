package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.entity.mob.SpecterEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class SpecterEntityRenderer extends BipedEntityRenderer<SpecterEntity, SpecterEntityRenderer.SpecterEntityModel> {

    public SpecterEntityRenderer(Context context) {
        super(context, new SpecterEntityModel(context.getPart(EntityModelLayers.PLAYER)), 0);
        addFeature(new ArmorFeatureRenderer<>(this,
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public Identifier getTexture(SpecterEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    static class SpecterEntityModel extends BipedEntityModel<SpecterEntity> {
        public SpecterEntityModel(ModelPart root) {
            super(root);
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
            // noop
        }
    }
}
