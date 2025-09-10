package com.minelittlepony.unicopia.client.particle;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.bezier.BezierSegment;
import com.minelittlepony.unicopia.client.render.bezier.Trail;
import com.minelittlepony.unicopia.particle.TargetBoundParticleEffect;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WindParticle extends AbstractBillboardParticle {
    private static final Identifier TEXTURE = Unicopia.id("textures/particle/wind.png");

    private final BezierSegment bezier = new BezierSegment();

    private final Trail trail;

    @Nullable
    private Entity target;

    private int attachmentTicks;

    private final Vec3d offset;
    private final boolean passive;

    public WindParticle(TargetBoundParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        trail = new Trail(new Vec3d(x, y, z), 0.02F);
        setMaxAge(300);
        this.alpha = 0.15F;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.attachmentTicks = (int)world.random.nextTriangular(15, 12);
        this.passive = effect.targetId() <= 0;
        this.collidesWithWorld = false;

        if (effect.targetId() > 0) {
            this.target = world.getEntityById(effect.targetId());
        }
        offset = target == null ? Vec3d.ZERO : new Vec3d(x, y, z).subtract(target.getPos());
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public boolean isAlive() {
        return age < getMaxAge() && (!dead || !trail.getSegments().isEmpty());
    }

    @Override
    protected void renderQuads(Tessellator te, float x, float y, float z, float tickDelta) {
        float alpha = this.alpha * (1 - (float)age / maxAge);
        int light = getBrightness(tickDelta);
        float scale = getScale(tickDelta);

        List<Trail.Segment> segments = trail.getSegments();

        @Nullable
        BufferBuilder buffer = null;

        for (int i = 0; i < segments.size() - 1; i++) {
            segments.get(i).getPlane(segments.get(i + 1), bezier);

            for (var corner : bezier.corners()) {
                corner.position().mul(scale).add(x, y, z);
            }

            if (buffer == null) {
                buffer = te.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            }

            quad(buffer, bezier.corners(), segments.get(i).getAlpha() * alpha, tickDelta, light);
        }

        if (buffer != null) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }
    }

    @Override
    public void tick() {
        super.tick();

        float animationFrame = age + MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);

        float sin = MathHelper.sin(animationFrame / 5F) * 0.1F;
        float cos = MathHelper.cos(animationFrame / 10F) * 0.2F;

        if (passive) {
            trail.update(new Vec3d(x + cos, y + sin, z - cos));
        } else {
            if (target != null && target.isAlive()) {

                trail.update(target.getPos().add(target.getRotationVecClient().multiply(-7)).add(offset).add(cos, sin, -cos));

                if (attachmentTicks > 0 && --attachmentTicks <= 0) {
                    target = null;
                }
            }
        }

        if (trail.tick()) {
            markDead();
        }
    }
}
