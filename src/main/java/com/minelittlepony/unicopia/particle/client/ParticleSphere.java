package com.minelittlepony.unicopia.particle.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import com.minelittlepony.unicopia.model.ModelSphere;
import com.minelittlepony.unicopia.particle.IAttachableParticle;
import com.minelittlepony.unicopia.spell.ICaster;
import com.minelittlepony.util.render.Color;

public class ParticleSphere extends Particle implements IAttachableParticle {

    private final float baseAlpha;

    protected int tint;
    protected float alpha;

    protected int radius;

    private ICaster<?> caster;

    private static final ModelSphere model = new ModelSphere();

    public ParticleSphere(int id, World w, double x, double y, double z, double vX, double vY, double vZ, int... args) {
        this(w, x, y, z, args[0], args[1], args[2]/255F);
    }

    public ParticleSphere(World w, double x, double y, double z, int radius, int tint, float alpha) {
        super(w, x, y, z);

        this.radius = radius;
        this.tint = tint;
        this.alpha = alpha;
        this.baseAlpha = alpha;

        this.setMaxAge(50000);
    }

    @Override
    public boolean isStillAlive() {
        return particleAge < (particleMaxAge - 1);
    }

    @Override
    public void attachTo(ICaster<?> caster) {
        this.caster = caster;
    }

    public void onUpdate() {
        super.onUpdate();

        alpha = Math.min(1F, 1 - (float)particleAge/particleMaxAge) * baseAlpha;

        if (caster == null || !caster.hasEffect() || caster.getEffect().getDead() || caster.getEntity().isDead) {
            setExpired();
        } else {
            Entity e = caster.getEntity();

            if (!caster.getWorld().loadedEntityList.contains(caster.getEntity())) {
                setExpired();
            }

            setPosition(e.posX, e.posY, e.posZ);
        }
    }

    public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float x, float z, float yz, float xy, float xz) {
        if (alpha <= 0) {
            return;
        }

        Color.glColor(tint, alpha);

        model.setPosition(posX, posY, posZ);
        model.render(radius);

        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public void setAttribute(int key, Object value) {
        if (key == 0) {
            radius = (int)value;
        }
    }
}

