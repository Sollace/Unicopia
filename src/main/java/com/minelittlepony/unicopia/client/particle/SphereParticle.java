package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.particle.ParticleHandle.Link;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.minelittlepony.common.util.Color;

public class SphereParticle extends Particle implements Attachment {

    protected float prevRadius;
    protected float radius;

    protected int steps;
    protected float lerpIncrement;
    protected float toRadius;

    private final Link link = new Link();

    private static final SphereModel MODEL = new SphereModel();

    public SphereParticle(SphereParticleEffect effect, ClientWorld w, double x, double y, double z, double vX, double vY, double vZ) {
        this(effect, w, x, y, z);

        this.velocityX = vX;
        this.velocityY = vY;
        this.velocityZ = vZ;
    }

    public SphereParticle(SphereParticleEffect effect, ClientWorld w, double x, double y, double z) {
        super(w, x, y, z);

        this.radius = effect.getRadius();
        this.colorRed = effect.getRed() / 255F;
        this.colorGreen = effect.getGreen() / 255F;
        this.colorBlue = effect.getBlue() / 255F;
        this.colorAlpha = effect.getAlpha();

        setMaxAge(10);
    }

    @Override
    public boolean isStillAlive() {
        return age < (maxAge - 1);
    }

    @Override
    public void attach(Caster<?> caster) {
        setMaxAge(50000);
        link.attach(caster);
    }

    @Override
    public void detach() {
        markDead();
    }

    @Override
    public void setAttribute(int key, Object value) {
        if (key == 0) {
            toRadius = (float)value;
            steps = 20;
            lerpIncrement = (toRadius - radius) / steps;
        }
        if (key == 1) {
            int tint = (int)value;
            colorRed = Color.r(tint);
            colorGreen = Color.g(tint);
            colorBlue = Color.b(tint);
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Override
    public void tick() {
        super.tick();

        if (link.linked()) {
            link.ifAbsent(this::markDead).map(Caster::getEntity).ifPresent(e -> {
                setPos(e.getX(), e.getY() + 0.5, e.getZ());

                prevPosX = e.lastRenderX;
                prevPosY = e.lastRenderY + 0.5;
                prevPosZ = e.lastRenderZ;
            });

            if (steps-- > 0) {
                radius += lerpIncrement;
            }
        } else {
            radius *= 0.9998281;
        }
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {

        if (colorAlpha <= 0 || radius <= 0) {
            return;
        }

        MODEL.setPosition(
                MathHelper.lerp(tickDelta, prevPosX, x) - camera.getPos().x,
                MathHelper.lerp(tickDelta, prevPosY, y) - camera.getPos().y,
                MathHelper.lerp(tickDelta, prevPosZ, z) - camera.getPos().z
        );
        MODEL.setRotation(0, 0, 0);

        float lerpedRad = MathHelper.lerp(tickDelta, prevRadius, radius);

        float[] color = ColorHelper.changeSaturation(colorRed, colorGreen, colorBlue, 4);
        RenderSystem.setShaderColor(color[0], color[1], color[2], colorAlpha / 3F);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(RenderLayers.getMagicGlow());

        float thickness = 0.05F;

        int light = getBrightness(tickDelta);

        MatrixStack matrices = new MatrixStack();
        MODEL.render(matrices, lerpedRad + thickness, buffer, light, 1, 1, 1, 1, 0.8F);
        MODEL.render(matrices, lerpedRad - thickness, buffer, light, 1, 1, 1, 1, 1);

        immediate.draw();

        prevRadius = radius;

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}

