package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.util.math.MathHelper;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.mojang.blaze3d.systems.RenderSystem;

public class SphereParticle extends Particle {
    static final int TRANSLUCENT_WHITE = Argb.withAlpha((int)(0.8F * 255), Colors.WHITE);

    protected float prevRadius;
    protected float radius;

    protected int steps;
    protected float lerpIncrement;
    protected float toRadius;

    public SphereParticle(SphereParticleEffect parameters, ClientWorld w, double x, double y, double z, double vX, double vY, double vZ) {
        this(parameters, w, x, y, z);

        this.velocityX = vX;
        this.velocityY = vY;
        this.velocityZ = vZ;
    }

    public SphereParticle(SphereParticleEffect parameters, ClientWorld w, double x, double y, double z) {
        super(w, x, y, z);
        this.radius = parameters.radius();
        this.red = parameters.color().x / 255F;
        this.green = parameters.color().y / 255F;
        this.blue = parameters.color().z / 255F;
        this.alpha = parameters.alpha();

        setMaxAge(10);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Override
    public void tick() {
        super.tick();

        radius *= 0.9998281;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {

        if (alpha <= 0 || radius <= 0) {
            return;
        }

        float[] color = ColorHelper.changeSaturation(red, green, blue, 4);
        RenderSystem.setShaderColor(color[0], color[1], color[2], alpha / 3F);
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(RenderLayers.getMagicNoColor());

        MatrixStack matrices = new MatrixStack();

        matrices.push();
        matrices.translate(
                MathHelper.lerp(tickDelta, prevPosX, x) - camera.getPos().x,
                MathHelper.lerp(tickDelta, prevPosY, y) - camera.getPos().y,
                MathHelper.lerp(tickDelta, prevPosZ, z) - camera.getPos().z
        );

        float scale = MathHelper.lerp(tickDelta, prevRadius, radius);

        renderModel(matrices, buffer, scale, tickDelta, getBrightness(tickDelta));

        matrices.pop();

        immediate.draw();

        prevRadius = radius;

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getParticleProgram);
    }

    protected void renderModel(MatrixStack matrices, VertexConsumer buffer, float lerpedRad, float tickDelta, int light) {
        float thickness = 0.05F;
        SphereModel.SPHERE.render(matrices, buffer, light, 1, lerpedRad + thickness, TRANSLUCENT_WHITE);
        SphereModel.SPHERE.render(matrices, buffer, light, 1, lerpedRad - thickness, Colors.WHITE);
    }
}

