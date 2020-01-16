package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.unicopia.particles.ParticleConnection.IAttachableParticle;
import com.mojang.blaze3d.platform.GlStateManager;

public class ParticleSphere extends Particle implements IAttachableParticle {

    protected int tint;
    protected float alpha;

    protected float radius;

    private ICaster<?> caster;

    private static final SphereModel model = new SphereModel();

    public ParticleSphere(ParticleEffect type, World w, double x, double y, double z, double vX, double vY, double vZ) {
        this(w, x, y, z, args[0] / 1000F, args[1], args[2]/100F);

        this.velocityX = vX;
        this.velocityY = vY;
        this.velocityZ = vZ;
    }

    public ParticleSphere(World w, double x, double y, double z, float radius, int tint, float alpha) {
        super(w, x, y, z);

        this.radius = radius;
        this.tint = tint;
        this.alpha = alpha;

        setMaxAge(10);
    }

    @Override
    public boolean isStillAlive() {
        return age < (maxAge - 1);
    }

    @Override
    public void attachTo(ICaster<?> caster) {
        setMaxAge(50000);
        this.caster = caster;
    }

    @Override
    public void buildGeometry(BufferBuilder var1, Camera var2, float var3, float var4, float var5, float var6,
            float var7, float var8) {
        // TODO Auto-generated method stub

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
            if (!caster.hasEffect() || caster.getEffect().getDead() || caster.getEntity().removed) {
                markDead();
            } else {
                Entity e = caster.getEntity();

                if (!caster.getWorld().loadedEntityList.contains(caster.getEntity())) {
                    markDead();
                }

                setPos(e.x, e.y, e.z);
            }
        } else {
            radius *= 0.9998281;
        }
    }

    public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float x, float z, float yz, float xy, float xz) {
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

        Color.glColor(tint, alpha);

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
            tint = (int)value;
        }
        if (key == 3) {
            alpha = (float)value;
        }
    }
}

