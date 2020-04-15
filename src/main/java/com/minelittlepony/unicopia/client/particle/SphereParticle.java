package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import com.minelittlepony.unicopia.client.render.SphereModel;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.util.particles.ParticleConnection.AttachableParticle;
import com.mojang.blaze3d.platform.GlStateManager;

public class SphereParticle extends Particle implements AttachableParticle {

    protected float red;
    protected float green;
    protected float blue;
    protected float alpha;

    protected float radius;

    private Caster<?> caster;

    private static final SphereModel model = new SphereModel();

    public SphereParticle(ParticleEffect type, World w,
            double x, double y, double z,
            float radius,
            int red, int green, int blue, float alpha,
            double vX, double vY, double vZ) {
        this(w, x, y, z, radius, red, green, blue, alpha);

        this.velocityX = vX;
        this.velocityY = vY;
        this.velocityZ = vZ;
    }

    public SphereParticle(World w, double x, double y, double z, float radius, int red, int green, int blue, float alpha) {
        super(w, x, y, z);

        this.radius = radius;
        this.red = red/255F;
        this.green = green/255F;
        this.blue = blue/255F;
        this.alpha = alpha;

        setMaxAge(10);
    }

    @Override
    public boolean isStillAlive() {
        return age < (maxAge - 1);
    }

    @Override
    public void attachTo(Caster<?> caster) {
        setMaxAge(50000);
        this.caster = caster;
    }

    @Override
    public ParticleTextureSheet getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (caster != null) {
            if (!caster.hasEffect() || caster.getEffect().isDead() || caster.getEntity().removed) {
                markDead();
            } else {
                Entity e = caster.getEntity();

                if (caster.getWorld().getEntityById(e.getEntityId()) == null) {
                    markDead();
                }

                setPos(e.x, e.y, e.z);
            }
        } else {
            radius *= 0.9998281;
        }
    }

    @Override
    public void buildGeometry(BufferBuilder buffer, Camera viewer, float partialTicks, float x, float z, float yz, float xy, float xz) {
        if (alpha <= 0 || radius <= 0) {
            return;
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.depthMask(false);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        MinecraftClient.getInstance().gameRenderer.disableLightmap();
        GlStateManager.enableLighting();

        GL14.glBlendColor(red, green, blue, alpha);

        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture();

        model.setPosition(this.x, this.y, this.z);
        model.render(radius);

        GlStateManager.enableTexture();
        GlStateManager.disableLighting();
        MinecraftClient.getInstance().gameRenderer.enableLightmap();

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.color4f(1, 1, 1, 1);
        GL11.glPopAttrib();
    }

    @Override
    public void setAttribute(int key, Object value) {
        if (key == 0) {
            radius = (float)value;
        }
        if (key == 1) {
            red = (int)value/255F;
        }
        if (key == 2) {
            green = (int)value/255F;
        }
        if (key == 3) {
            blue = (int)value/255F;
        }
        if (key == 4) {
            alpha = (float)value;
        }
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider provider;

        public Factory(SpriteProvider provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType_1, World world, double x, double y, double z, double dx, double dy, double dz) {
            RaindropsParticle particle = new RaindropsParticle(world, x, y, z, dx, dy, dz);
            particle.setSprite(provider);
            return particle;
        }
    }
}

