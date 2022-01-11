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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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

    private final SphereParticleEffect parameters;

    public SphereParticle(SphereParticleEffect parameters, ClientWorld w, double x, double y, double z, double vX, double vY, double vZ) {
        this(parameters, w, x, y, z);

        this.velocityX = vX;
        this.velocityY = vY;
        this.velocityZ = vZ;
    }

    public SphereParticle(SphereParticleEffect parameters, ClientWorld w, double x, double y, double z) {
        super(w, x, y, z);
        this.parameters = parameters;
        this.radius = parameters.getRadius();
        this.colorRed = parameters.getColor().getX() / 255F;
        this.colorGreen = parameters.getColor().getY() / 255F;
        this.colorBlue = parameters.getColor().getZ() / 255F;
        this.colorAlpha = parameters.getAlpha();

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
        if (key == 2) {
            colorAlpha = (float)value;
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
                Vec3d offset = parameters.getOffset();
                setPos(e.getX() + offset.getX(), e.getY() + offset.getY(), e.getZ() + offset.getZ());

                prevPosX = e.lastRenderX + offset.getX();
                prevPosY = e.lastRenderY + offset.getY();
                prevPosZ = e.lastRenderZ + offset.getZ();
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

        float[] color = ColorHelper.changeSaturation(colorRed, colorGreen, colorBlue, 4);
        RenderSystem.setShaderColor(color[0], color[1], color[2], colorAlpha / 3F);

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

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getParticleShader);
    }

    protected void renderModel(MatrixStack matrices, VertexConsumer buffer, float lerpedRad, float tickDelta, int light) {
        float thickness = 0.05F;
        SphereModel.SPHERE.render(matrices, buffer, light, 1, lerpedRad + thickness, 1, 1, 1, 0.8F);
        SphereModel.SPHERE.render(matrices, buffer, light, 1, lerpedRad - thickness, 1, 1, 1, 1);
    }
}

