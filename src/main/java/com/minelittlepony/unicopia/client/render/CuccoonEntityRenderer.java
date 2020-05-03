package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.client.render.model.CuccoonEntityModel;
import com.minelittlepony.unicopia.entity.CuccoonEntity;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class CuccoonEntityRenderer extends LivingEntityRenderer<CuccoonEntity, CuccoonEntityModel> {

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/entity/cuccoon.png");

    public CuccoonEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager, new CuccoonEntityModel(), 1);
    }

    @Override
    public Identifier getTexture(CuccoonEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(CuccoonEntity entity) {
        return 0;
    }

    @Override
    public void render(CuccoonEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        if (entity.hasPassengers()) {
            Entity rider = entity.getPassengerList().get(0);

            if (!(rider == MinecraftClient.getInstance().player) || InteractionManager.instance().getViewMode() != 0) {
                RenderSystem.enableAlphaTest();
                RenderSystem.enableBlend();

                renderManager.render(rider, rider.getX(), rider.getY() + rider.getMountedHeightOffset(), rider.getZ(), rider.yaw, tickDelta, matrices, vertices, light);

                RenderSystem.disableBlend();
                RenderSystem.disableAlphaTest();
            }
        }

        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }
}
