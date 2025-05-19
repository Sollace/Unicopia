package com.minelittlepony.unicopia.client.particle;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.particle.FootprintParticleEffect;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FootprintParticle extends SpriteBillboardParticle {
    // specter

    public FootprintParticle(FootprintParticleEffect effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, 0, 0, 0);
        setVelocity(0, 0, 0);
        setSprite(provider.getSprite(world.random));
        this.angle = effect.yaw() * MathHelper.RADIANS_PER_DEGREE;
        this.maxAge = 1000;
        this.gravityStrength = 1;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void buildGeometry(VertexConsumer drawer, Camera camera, float tickDelta) {
        Vec3d cam = camera.getPos();

        float renderX = (float)(MathHelper.lerp(tickDelta, prevPosX, x) - cam.getX());
        float renderY = (float)(MathHelper.lerp(tickDelta, prevPosY, y) - cam.getY());
        float renderZ = (float)(MathHelper.lerp(tickDelta, prevPosZ, z) - cam.getZ());

        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1, 0, -1),
                new Vector3f(-1, 0, 1),
                new Vector3f( 1, 0, 1),
                new Vector3f( 1, 0, -1)
        };
        for (int k = 0; k < 4; ++k) {
           Vector3f corner = corners[k];
           corner.mul(0.2F);
           corner.rotateAxis(angle, 0, 1, 0);
           corner.add(renderX, renderY + 0.0001F, renderZ);
        }

        float alpha = this.alpha * (1 - ((float)age / maxAge));
        int light = getBrightness(tickDelta);

        float minU = this.sprite.getMinU();
        float maxU = this.sprite.getMaxU();

        float minV = this.sprite.getMinV();
        float maxV = this.sprite.getMaxV();

        drawer.vertex(corners[0].x, corners[0].y, corners[0].z).texture(minU, minV).color(red, green, blue, alpha).light(light);
        drawer.vertex(corners[1].x, corners[1].y, corners[1].z).texture(maxU, minV).color(red, green, blue, alpha).light(light);
        drawer.vertex(corners[2].x, corners[2].y, corners[2].z).texture(maxU, maxV).color(red, green, blue, alpha).light(light);
        drawer.vertex(corners[3].x, corners[3].y, corners[3].z).texture(minU, maxV).color(red, green, blue, alpha).light(light);
    }

}
