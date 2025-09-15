package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.AbstractBoatEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BoatEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BoatEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.Identifier;

public class CustomBoatEntityRenderer<T extends AbstractBoatEntity> extends AbstractBoatEntityRenderer {
    private final Model waterMaskModel;
    private final Identifier texture;
    private final EntityModel<BoatEntityRenderState> model;

    public CustomBoatEntityRenderer(EntityRendererFactory.Context ctx, EntityType<T> type, boolean chest) {
        super(ctx);
        texture = EntityType.getId(type).withPath(path -> "textures/entity/" + (chest ? "chest_boat" : "boat") + "/" + path.replace(chest ? "_chest_boat" : "_boat", "") + ".png");
        waterMaskModel = new Model.SinglePartModel(ctx.getPart(EntityModelLayers.BOAT), id -> RenderLayer.getWaterMask());
        model = new BoatEntityModel((chest ? BoatEntityModel.getChestTexturedModelData() : BoatEntityModel.getTexturedModelData()).createModel());
    }

    @Override
    protected EntityModel<BoatEntityRenderState> getModel() {
        return model;
    }

    @Override
    protected RenderLayer getRenderLayer() {
        return model.getLayer(texture);
    }

    @Override
    protected void renderWaterMask(BoatEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!state.submergedInWater) {
            waterMaskModel.render(matrices, vertexConsumers.getBuffer(waterMaskModel.getLayer(texture)), light, OverlayTexture.DEFAULT_UV);
        }
    }
}
