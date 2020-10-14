package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class RainboomParticle extends OrientedBillboardParticle {
    private static final Identifier TEXTURE = new Identifier("unicopia", "textures/particles/rainboom_ring.png");

    protected float prevBaseSize = 0;
    protected float baseSize = 0;

    public RainboomParticle(OrientedBillboardParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(effect, world, x, y, z, velocityX, velocityY, velocityZ);
        setMaxAge(40);
    }

    @Override
    public float getScale(float tickDelta) {
       return MathHelper.lerp(tickDelta, prevBaseSize, baseSize) * super.getScale(tickDelta);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public void tick() {
        super.tick();

        prevBaseSize = baseSize;
        baseSize++;

        if (age == 1) {
            world.playSound(x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 5, 0.3F, true);
            world.playSound(x, y, z, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 10, 1.3F, true);
        }
    }
}
