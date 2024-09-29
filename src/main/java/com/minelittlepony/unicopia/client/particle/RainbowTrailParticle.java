package com.minelittlepony.unicopia.client.particle;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.client.render.bezier.BezierSegment;
import com.minelittlepony.unicopia.client.render.bezier.Trail;
import com.minelittlepony.unicopia.particle.TargetBoundParticleEffect;

import net.minecraft.client.render.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RainbowTrailParticle extends AbstractBillboardParticle {
    private static final Identifier TEXTURE = Unicopia.id("textures/particles/rainboom_trail.png");

    private final Trail trail;

    @Nullable
    private Entity target;
    private boolean isAbility;

    public RainbowTrailParticle(TargetBoundParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        trail = new Trail(new Vec3d(x, y, z), 1);
        setMaxAge(300);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;

        if (effect.targetId() <= 0) {
            this.target = world.getOtherEntities(null, Box.from(trail.pos)).get(0);
        } else {
            this.target = world.getEntityById(effect.targetId());
        }
        isAbility = Caster.of(target).filter(caster -> SpellType.RAINBOOM.isOn(caster)).isPresent();
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

        List<Trail.Segment> segments = trail.getSegments();

        for (int i = 0; i < segments.size() - 1; i++) {
            BezierSegment corners = segments.get(i).getPlane(segments.get(i + 1));
            float scale = getScale(tickDelta);

            corners.forEachCorner(corner -> {
                corner.position().mul(scale).add(x, y, z);
            });

            renderQuad(te, corners.corners(), segments.get(i).getAlpha() * alpha, tickDelta);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (target != null && target.isAlive()) {
            if (isAbility) {
                age = 0;
            }
            trail.update(target.getEyePos());

            if (isAbility && Caster.of(target).filter(caster -> SpellType.RAINBOOM.isOn(caster)).isEmpty()) {
                target = null;
            }
        }

        if (trail.tick()) {
            markDead();
        }
    }
}
