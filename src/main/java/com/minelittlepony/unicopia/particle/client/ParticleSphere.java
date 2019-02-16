package com.minelittlepony.unicopia.particle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import com.minelittlepony.unicopia.model.ModelSphere;
import com.minelittlepony.unicopia.particle.IAttachableParticle;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.util.render.Color;

public class ParticleSphere extends Particle implements IAttachableParticle {

    protected int tint;
    protected float alpha;

    protected float radius;

    private ICaster<?> caster;

    private static final ModelSphere model = new ModelSphere();

    public ParticleSphere(int id, World w, double x, double y, double z, double vX, double vY, double vZ, int... args) {
        this(w, x, y, z, args[0] / 1000F, args[1], args[2]/100F);

        this.motionX = vX;
        this.motionY = vY;
        this.motionZ = vZ;
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
        return particleAge < (particleMaxAge - 1);
    }

    @Override
    public void attachTo(ICaster<?> caster) {
        setMaxAge(50000);
        this.caster = caster;
    }

    public void onUpdate() {
        super.onUpdate();

        if (caster != null) {
            if (!caster.hasEffect() || caster.getEffect().getDead() || caster.getEntity().isDead) {
                setExpired();
            } else {
                Entity e = caster.getEntity();

                if (!caster.getWorld().loadedEntityList.contains(caster.getEntity())) {
                    setExpired();
                }

                setPosition(e.posX, e.posY, e.posZ);
            }
        } else {
            radius *= 0.9998281;
        }
    }

    public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float x, float z, float yz, float xy, float xz) {
        if (alpha <= 0 || radius <= 0) {
            return;
        }

        GlStateManager.depthMask(false);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GlStateManager.enableBlend();
        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.enableLighting();

        Color.glColor(tint, alpha);

        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();

        model.setPosition(posX, posY, posZ);
        model.render(radius);

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        Minecraft.getMinecraft().entityRenderer.enableLightmap();

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.color(1, 1, 1, 1);
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

