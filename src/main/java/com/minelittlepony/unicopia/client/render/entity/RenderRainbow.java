package com.minelittlepony.unicopia.client.render.entity;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.entity.EntityRainbow;
import com.minelittlepony.util.WorldHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Identifier;

public class RenderRainbow extends Render<EntityRainbow> {

    public RenderRainbow(RenderManager renderManager) {
        super(renderManager);
    }

    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/environment/rainbow.png");

    public void doRender(EntityRainbow entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float distance = MinecraftClient.getInstance().getRenderViewEntity().getDistance(entity);
        float maxDistance = 16 * MinecraftClient.getInstance().gameSettings.renderDistanceChunks;
        double r = entity.getRadius();
        float light = WorldHelper.getDaylightBrightness(entity.getEntityWorld(), partialTicks);

        float opacity = ((maxDistance - distance) / maxDistance);

        opacity *= light;

        if (opacity <= 0) {
            return;
        }

        bindEntityTexture(entity);

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(entityYaw, 0, 1, 0);

        GlStateManager.color(1, 1, 1, opacity);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-r, r, 0).tex(1, 0).endVertex();
        bufferbuilder.pos( r, r, 0).tex(0, 0).endVertex();
        bufferbuilder.pos( r, 0, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(-r, 0, 0).tex(1, 1).endVertex();

        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    protected Identifier getEntityTexture(EntityRainbow entity) {
        return TEXTURE;
    }

}
