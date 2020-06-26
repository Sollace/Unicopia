package com.minelittlepony.unicopia.world.client.render;

import com.minelittlepony.unicopia.world.client.render.model.ButterflyEntityModel;
import com.minelittlepony.unicopia.world.entity.ButterflyEntity;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ButterflyEntityRenderer extends MobEntityRenderer<ButterflyEntity, ButterflyEntityModel> {

    public ButterflyEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new ButterflyEntityModel(), 0.25F);
    }

    @Override
    public Identifier getTexture(ButterflyEntity entity) {
        return entity.getVariety().getSkin();
    }

    @Override
    protected void scale(ButterflyEntity entity, MatrixStack matrices, float ticks) {
        matrices.scale(0.35F, 0.35F, 0.35F);
        matrices.translate(0.8F, 0, -0.5F);
    }

    @Override
    protected void setupTransforms(ButterflyEntity entity, MatrixStack matrices, float age, float yaw, float ticks) {

        if (!entity.isResting()) {
            matrices.translate(0, MathHelper.cos(age / 3F) / 10F, 0);
        }

        super.setupTransforms(entity, matrices, age, yaw, ticks);
    }
}
