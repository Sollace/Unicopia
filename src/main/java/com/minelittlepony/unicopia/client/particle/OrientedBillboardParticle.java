package com.minelittlepony.unicopia.client.particle;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.minelittlepony.unicopia.particle.OrientedBillboardParticleEffect;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;

public abstract class OrientedBillboardParticle extends AbstractBillboardParticle {

    protected boolean fixed;
    protected Quaternionf rotation = new Quaternionf(0, 0, 0, 1);

    public OrientedBillboardParticle(OrientedBillboardParticleEffect effect, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        fixed = effect.fixed();
        if (fixed) {
            // Was hamiltonianProduct (CHECK THIS!!)
            rotation.mul(RotationAxis.POSITIVE_Y.rotationDegrees(effect.pitch()));
            rotation.mul(RotationAxis.POSITIVE_X.rotationDegrees(180 - effect.yaw()));
        }
    }

    @Override
    public void buildGeometry(VertexConsumer drawer, Camera camera, float tickDelta) {
        if (!fixed) {
            rotation = camera.getRotation();
        }
        super.buildGeometry(drawer, camera, tickDelta);
    }

    @Override
    protected void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta) {
        Vector3f[] corners = new Vector3f[]{
                new Vector3f(-1, -1, 0),
                new Vector3f(-1,  1, 0),
                new Vector3f( 1,  1, 0),
                new Vector3f( 1, -1, 0)
        };
        float scale = getScale(tickDelta);

        for(int k = 0; k < 4; ++k) {
           Vector3f corner = corners[k];
           corner.rotate(rotation);
           corner.mul(scale);
           corner.add(x, y, z);
        }

        float alpha = this.alpha * (1 - ((float)age / maxAge));

        renderQuad(te, buffer, corners, alpha, tickDelta);
    }

}
