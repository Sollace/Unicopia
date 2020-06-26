package com.minelittlepony.unicopia.world.client.render;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.util.WorldHelper;
import com.minelittlepony.unicopia.world.entity.RainbowEntity;
import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.Identifier;

public class RainbowEntityRenderer extends EntityRenderer<RainbowEntity> {
    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/environment/rainbow.png");

    public RainbowEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager);
    }

    @Override
    public Identifier getTexture(RainbowEntity entity) {
        return TEXTURE;
    }

    public void render(RainbowEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float distance = MinecraftClient.getInstance().getCameraEntity().distanceTo(entity);
        float maxDistance = 16 * MinecraftClient.getInstance().options.viewDistance;
        double r = entity.getRadius();
        float light = WorldHelper.getDaylightBrightness(entity.getEntityWorld(), partialTicks);

        float opacity = ((maxDistance - distance) / maxDistance);

        opacity *= light;

        if (opacity <= 0) {
            return;
        }

        RenderSystem.pushMatrix();
        RenderSystem.disableLighting();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.translated(x, y, z);
        RenderSystem.rotatef(entityYaw, 0, 1, 0);

        RenderSystem.color4f(1, 1, 1, opacity);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
        bufferbuilder.vertex(-r, r, 0).texture(1, 0).next();
        bufferbuilder.vertex( r, r, 0).texture(0, 0).next();
        bufferbuilder.vertex( r, 0, 0).texture(0, 1).next();
        bufferbuilder.vertex(-r, 0, 0).texture(1, 1).next();

        tessellator.draw();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableLighting();
        RenderSystem.popMatrix();
    }
}
