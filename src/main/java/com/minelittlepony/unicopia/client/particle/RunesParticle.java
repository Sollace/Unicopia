package com.minelittlepony.unicopia.client.particle;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

@Deprecated
public class RunesParticle extends OrientedBillboardParticle {

    private static final Identifier[] TEXTURES = new Identifier[] {
            Unicopia.id("textures/particles/runes_0.png"),
            Unicopia.id("textures/particles/runes_1.png"),
            Unicopia.id("textures/particles/runes_2.png"),
            Unicopia.id("textures/particles/runes_3.png"),
            Unicopia.id("textures/particles/runes_4.png"),
            Unicopia.id("textures/particles/runes_5.png")
    };

    protected float targetSize = 3;

    protected float prevBaseSize = 0;
    protected float baseSize = 0;

    private float prevRotationAngle;
    private float rotationAngle;

    public RunesParticle(OrientedBillboardParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(effect, world, x, y, z, velocityX, velocityY, velocityZ);
        setMaxAge(70);

        red = world.random.nextFloat();
        green = world.random.nextFloat();
        blue = world.random.nextFloat();
    }

    @Override
    public float getScale(float tickDelta) {
       return MathHelper.lerp(tickDelta, prevBaseSize, baseSize) * super.getScale(tickDelta);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURES[0];
    }

    private float getAlphaScale() {
        float transitionScale = age < maxAge / 2 ? 5 : 3;
        return (float)Math.min(1, Math.sin(Math.PI * age / maxAge) * transitionScale);
    }

    @Override
    protected int getBrightness(float tint) {
        return 0xF000F0;
    }

    @Override
    protected void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta) {

        float alpha = this.alpha * getAlphaScale();

        float angle = MathHelper.lerp(tickDelta, prevRotationAngle, rotationAngle);

        for (int i = 0; i < TEXTURES.length; i++) {
            for (int dim = 0; dim < 3; dim++) {
                RenderSystem.setShaderTexture(0, TEXTURES[i]);
                RenderSystem.setShaderColor(red, green, blue, alpha / ((float)(dim * 3) + 1));

                Vector3f[] corners = new Vector3f[]{
                        new Vector3f(-1, -1, 0),
                        new Vector3f(-1,  1, 0),
                        new Vector3f( 1,  1, 0),
                        new Vector3f( 1, -1, 0)
                };
                float scale = getScale(tickDelta);

                float ringSpeed = (i % 2 == 0 ? i : -1) * i;

                Quaternionf ringAngle = RotationAxis.POSITIVE_Z.rotationDegrees(angle * ringSpeed);
                Quaternionf ringFlip = RotationAxis.POSITIVE_Y.rotationDegrees(angle * ringSpeed * dim);
                Quaternionf ringRoll = RotationAxis.POSITIVE_X.rotationDegrees(angle * ringSpeed * dim);

                for(int k = 0; k < 4; ++k) {
                   Vector3f corner = corners[k];
                   corner.rotate(ringAngle);
                   corner.rotate(ringFlip);
                   corner.rotate(ringRoll);
                   corner.rotate(rotation);
                   corner.mul(scale);
                   corner.add(x, y + 0.001F, z);
                }

                renderQuad(te, buffer, corners, alpha, tickDelta);
            }
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public void tick() {
        super.tick();

        prevBaseSize = baseSize;
        if (baseSize < targetSize) {
            baseSize += 0.1F;
        }
        if (baseSize > targetSize) {
            baseSize -= 0.1F;
        }

        rotationAngle = (rotationAngle + 0.3F) % 360;
        prevRotationAngle = rotationAngle - 0.3F;
    }
}
