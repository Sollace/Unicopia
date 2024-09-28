package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GroundPoundParticle extends Particle {

    public GroundPoundParticle(SimpleParticleType effect, ClientWorld world, double x, double y, double z, double dX, double dY, double dZ) {
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

    protected Vec3d getPos() {
        return new Vec3d(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();
        spawnChildParticles();
    }

    protected void spawnChildParticles() {
        Vec3d vel = new Vec3d(0, (0.5 + (Math.sin(age) * 2.5)) * 5, 0);

        new Sphere(true, age, 1, 0, 1).translate(getPos()).randomPoints(random).forEach(point -> {
            BlockPos pos = BlockPos.ofFloored(point).down();

            BlockState state = world.getBlockState(pos);
            if (state.isAir()) {
                state = world.getBlockState(pos.down());
                if (state.isAir()) {
                    state = Blocks.DIRT.getDefaultState();
                }
            }

            ParticleUtils.spawnParticle(world, new BlockStateParticleEffect(ParticleTypes.BLOCK, state), point, vel);
        });
    }
}
