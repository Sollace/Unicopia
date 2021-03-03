package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleHandle.Attachment;
import com.minelittlepony.unicopia.particle.ParticleHandle.Link;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;

public class RunesParticle extends OrientedBillboardParticle implements Attachment {

    private static final Identifier[] TEXTURES = new Identifier[] {
            new Identifier("unicopia", "textures/particles/runes_0.png"),
            new Identifier("unicopia", "textures/particles/runes_1.png"),
            new Identifier("unicopia", "textures/particles/runes_2.png"),
            new Identifier("unicopia", "textures/particles/runes_3.png"),
            new Identifier("unicopia", "textures/particles/runes_4.png"),
            new Identifier("unicopia", "textures/particles/runes_5.png")
    };

    protected float prevBaseSize = 0;
    protected float baseSize = 0;

    private float prevRotationAngle;
    private float rotationAngle;

    private final Link link = new Link();

    private int stasisAge = -1;

    public RunesParticle(OrientedBillboardParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(effect, world, x, y, z, velocityX, velocityY, velocityZ);
        setMaxAge(70);

        colorRed = world.random.nextFloat();
        colorGreen = world.random.nextFloat();
        colorBlue = world.random.nextFloat();
    }

    @Override
    public boolean isStillAlive() {
        return age < (maxAge - 1);
    }

    @Override
    public void attach(Caster<?> caster) {
        link.attach(caster);
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
    }

    @Override
    public void detach() {
        link.detach();
    }

    @Override
    public void setAttribute(int key, Object value) {
        if (key == 1) {
            int tint = (int)value;
            colorRed = Color.r(tint);
            colorGreen = Color.g(tint);
            colorBlue = Color.b(tint);
        }
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
    protected int getColorMultiplier(float tint) {
        return 0xF000F0;
    }

    @Override
    protected void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta) {

        float alpha = colorAlpha * getAlphaScale();

        float angle = MathHelper.lerp(tickDelta, prevRotationAngle, rotationAngle);

        for (int i = 0; i < TEXTURES.length; i++) {
            bindTexture(TEXTURES[i]);

            RenderSystem.color3f(colorRed, colorGreen, colorBlue);

            Vector3f[] corners = new Vector3f[]{
                    new Vector3f(-1, -1, 0),
                    new Vector3f(-1,  1, 0),
                    new Vector3f( 1,  1, 0),
                    new Vector3f( 1, -1, 0)
            };
            float scale = getScale(tickDelta);

            float ringSpeed = (i % 2 == 0 ? i : -1) * i;

            Quaternion ringAngle = Vector3f.POSITIVE_Z.getDegreesQuaternion(angle * ringSpeed);

            for(int k = 0; k < 4; ++k) {
               Vector3f corner = corners[k];
               corner.rotate(ringAngle);
               corner.rotate(rotation);
               corner.scale(scale);
               corner.add(x, y + 0.001F, z);
            }

            renderQuad(te, buffer, corners, alpha, tickDelta);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (link.linked()) {
            link.ifAbsent(this::detach).map(Caster::getEntity).ifPresent(e -> {
                if (getAlphaScale() >= 0.9F) {
                    if (stasisAge < 0) {
                        stasisAge = age;
                    }
                    age = stasisAge;
                }
            });
        }

        prevBaseSize = baseSize;
        if (baseSize < 3) {
            baseSize++;
        }

        prevRotationAngle = rotationAngle;
        rotationAngle = MathHelper.wrapDegrees(rotationAngle + 0.3F);
    }
}
