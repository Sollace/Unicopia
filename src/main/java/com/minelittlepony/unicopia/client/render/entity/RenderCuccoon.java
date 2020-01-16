package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.model.ModelCuccoon;
import com.minelittlepony.unicopia.entity.EntityCuccoon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class RenderCuccoon extends RenderLivingBase<EntityCuccoon> {

    private static final Identifier TEXTURE = new Identifier(Unicopia.MODID, "textures/entity/cuccoon.png");

    public RenderCuccoon(RenderManager manager) {
        super(manager, new ModelCuccoon(), 1);
    }

    @Override
    protected Identifier getEntityTexture(EntityCuccoon entity) {
        return TEXTURE;
    }

    @Override
    protected float getDeathMaxRotation(EntityCuccoon entity) {
        return 0;
    }

    @Override
    public void doRender(EntityCuccoon entity, double x, double y, double z, float entityYaw, float partialTicks) {

        if (entity.isBeingRidden()) {
            Entity rider = entity.getPassengers().get(0);

            if (!(rider == MinecraftClient.getInstance().player) || UClient.instance().getViewMode() != 0) {
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();

                renderManager.renderEntity(rider, x, y + rider.getYOffset(), z, entityYaw, partialTicks, true);

                GlStateManager.disableBlend();
                GlStateManager.disableAlpha();
            }


        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
