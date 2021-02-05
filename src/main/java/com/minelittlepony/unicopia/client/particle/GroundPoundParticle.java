package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GroundPoundParticle extends Particle {

    public GroundPoundParticle(DefaultParticleType effect, ClientWorld world, double x, double y, double z, double dX, double dY, double dZ) {
        super(world, x, y, z, dX, dY, dZ);
        maxAge = 10;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.NO_RENDER;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float f) {
    }

    @Override
    public void tick() {
        super.tick();

        spawnParticleRing(age, 1);
    }


    private void spawnParticleRing(int timeDiff, double yVel) {

        Shape shape = new Sphere(true, timeDiff, 1, 0, 1);

        double y = 0.5 + (Math.sin(timeDiff) * 2.5);

        yVel *= y * 5;

        Vec3d center = new Vec3d(x, this.y, z);
        for (int i = 0; i < shape.getVolumeOfSpawnableSpace(); i++) {
            Vec3d point = shape.computePoint(random).add(center);

            BlockPos pos = new BlockPos(point.x, center.y - 1, point.z);

            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                state = world.getBlockState(pos.down());
                if (state.isAir()) {
                    state = Blocks.DIRT.getDefaultState();
                }
            }

            world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    point.x,
                    point.y,
                    point.z,
                    0, yVel, 0
            );
        }
    }

}
