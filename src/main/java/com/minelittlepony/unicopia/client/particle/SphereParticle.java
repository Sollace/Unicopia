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

import java.util.Optional;

import com.minelittlepony.common.util.Color;

public class SphereParticle extends Particle implements Attachment {

    protected float prevRadius;
    protected float radius;

    protected int steps;
    protected float lerpIncrement;
    protected float toRadius;

    private Optional<Link> link = Optional.empty();

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
        this.colorRed = effect.getColor().getX() / 255F;
        this.colorGreen = effect.getColor().getY() / 255F;
        this.colorBlue = effect.getColor().getZ() / 255F;
        this.colorAlpha = effect.getAlpha();

        setMaxAge(10);
    }

    @Override
    public boolean isStillAlive() {
        return age < (maxAge - 1);
    }

    @Override
    public void attach(Link link) {
        setMaxAge(50000);
        this.link = Optional.of(link);
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

        if (link.isPresent()) {
            link.flatMap(Link::get).map(Caster::getEntity).ifPresentOrElse(e -> {
                setPos(e.getX(), e.getY() + 0.5, e.getZ());

                prevPosX = e.lastRenderX;
                prevPosY = e.lastRenderY + 0.5;
                prevPosZ = e.lastRenderZ;
            }, this::detach);

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

        float lerpedRad = MathHelper.lerp(tickDelta, prevRadius, radius);

        float[] color = ColorHelper.changeSaturation(colorRed, colorGreen, colorBlue, 4);
        RenderSystem.setShaderColor(color[0], color[1], color[2], colorAlpha / 3F);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(RenderLayers.getMagicGlow());

        float thickness = 0.05F;

        int light = getBrightness(tickDelta);

        MatrixStack matrices = new MatrixStack();

        matrices.push();
        matrices.translate(
                MathHelper.lerp(tickDelta, prevPosX, x) - camera.getPos().x,
                MathHelper.lerp(tickDelta, prevPosY, y) - camera.getPos().y,
                MathHelper.lerp(tickDelta, prevPosZ, z) - camera.getPos().z
        );

        float scale = lerpedRad + thickness;

        if (scale > 0) {
            matrices.push();
            matrices.scale(scale, scale, scale);
            MODEL.render(matrices, buffer, light, 1, 1, 1, 1, 0.8F);
            matrices.pop();
        }

        scale = lerpedRad - thickness;

        if (scale > 0) {
            matrices.scale(scale, scale, scale);

            MODEL.render(matrices, buffer, light, 1, 1, 1, 1, 1);
        }
        matrices.pop();

        immediate.draw();

        prevRadius = radius;

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}

