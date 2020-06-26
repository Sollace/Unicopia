package com.minelittlepony.unicopia.particles;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ParticleEmitter {

    static final ParticleEmitter INSTANCE = new ParticleEmitter();

    public static ParticleEmitter instance() {
        return INSTANCE;
    }

    private static final Vec3d BUFFER = new Vec3d(0.25, 0.25, 0.25);
    private static final Vec3d HALF = new Vec3d(0.5, 0.5, 0.5);

    private static final EnumSet<Axis> ALL_EXES = EnumSet.allOf(Axis.class);

    public void emitDestructionParticles(Entity entity, BlockState state) {

        float height = entity.getHeight();
        float width = entity.getWidth();

        Vec3d dim = new Vec3d(width, height, width).add(BUFFER);
        Vec3d origin = entity.getPos();

        int total = (int)(32 * dim.length());

        for (int i = 0; i < total; i++) {
            Vec3d pos = centeredRange(entity.world.random, origin, dim);
            Vec3d vel = pos.subtract(pos.floorAlongAxes(ALL_EXES).subtract(HALF));

            spawnDiggingFX(entity.world, pos, vel, state, 1);
        }
    }

    public void emitDiggingParticles(Entity entity, Block block) {
        emitDiggingParticles(entity, block.getDefaultState());
    }

    public void emitDiggingParticles(Entity entity, BlockState state) {

        Vec3d area = new Vec3d(entity.getWidth(), entity.getHeight(), entity.getWidth()).add(BUFFER);
        Vec3d origin = entity.getPos();

        for (Direction side : Direction.values()) {
            Vec3d plane = Vec3d.of(side.getVector());

            spawnDiggingFX(entity.getEntityWorld(),
                    clampPlane(side.getAxis(),
                            centeredRange(entity.world.random, origin, area),
                            origin.add(area.multiply(plane))
                    ),
                    plane.multiply(0.5), state, 0.6F);
        }
    }

    private Vec3d clampPlane(Axis axis, Vec3d vector, Vec3d plane) {
        return new Vec3d(
                axis == Axis.X ? plane.x : vector.x,
                axis == Axis.Y ? plane.y : vector.y,
                axis == Axis.Z ? plane.z : vector.z
        );
    }

    private Vec3d centeredRange(Random rand, Vec3d center, Vec3d max) {
        return center
                .add(new Vec3d(
                            rand.nextFloat(),
                            rand.nextFloat(), rand.nextFloat()
                    ).multiply(max))
                .subtract(max.multiply(0.5D));
    }

    protected void spawnDiggingFX(World w, Vec3d pos, Vec3d vel, BlockState blockState, float multVel) {
        if (w instanceof ServerWorld) {

            double speed = vel.length() * multVel;
            vel = vel.normalize();

            ((ServerWorld)w).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState),
                    pos.x, pos.y, pos.z, 1,
                    vel.x, vel.y, vel.z,
                    speed
            );
        }
    }
}
