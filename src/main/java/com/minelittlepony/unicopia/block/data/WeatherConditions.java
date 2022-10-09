package com.minelittlepony.unicopia.block.data;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.World;

public class WeatherConditions {
    public static final TwoDimensionalField HEIGHT_MAP_FIELD = (world, pos) -> {
        return world.getTopY(Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
    };
    public static final TwoDimensionalField THERMAL_FIELD = (world, pos) -> {
        return world.getBiome(pos).value().getTemperature() + (float)getUpdraft(pos, world);
    };
    public static final TwoDimensionalField LOCAL_ALTITUDE_FIELD = (world, pos) -> {
        if (!world.isAir(pos)) {
            return 0;
        }
        return pos.getY() - getSurfaceBelow(pos, world);
    };

    public static final double FIRE_UPDRAFT = 0.3;
    public static final double SAND_UPDRAFT = 0.13;
    public static final double SOUL_SAND_UPDRAFT = -0.13;
    public static final double ICE_UPDRAFT = -0.10;
    public static final double VOID_UPDRAFT = -0.23;

    public static final float MAX_UPDRAFT_HEIGHT = 20;
    public static final float MAX_TERRAIN_HEIGHT = 50;

    public static Vec3d getAirflow(BlockPos pos, World world) {
        BlockPos.Mutable probedPosition = new BlockPos.Mutable();

        final float terrainFactor = Math.min(MAX_TERRAIN_HEIGHT, LOCAL_ALTITUDE_FIELD.getValue(world, probedPosition.set(pos))) / MAX_TERRAIN_HEIGHT;

        Vec3d terrainGradient = LOCAL_ALTITUDE_FIELD.computeAverage(world, pos, probedPosition).multiply(1 - terrainFactor);
        Vec3d thermalGradient = THERMAL_FIELD.computeAverage(world, pos, probedPosition).multiply(terrainFactor);

        return terrainGradient.add(thermalGradient).normalize().add(0, getUpdraft(probedPosition.set(pos), world), 0);
    }

    public static double getUpdraft(BlockPos.Mutable pos, World world) {
        final float ratio = 1 - Math.min(MAX_UPDRAFT_HEIGHT, pos.getY() - getSurfaceBelow(pos, world)) / MAX_UPDRAFT_HEIGHT;

        BlockState state = world.getBlockState(pos);
        if (state.isAir()) {
            return VOID_UPDRAFT * ratio;
        }

        if (state.isOf(Blocks.SOUL_SAND) || state.isOf(Blocks.SOUL_SOIL)) {
            return SOUL_SAND_UPDRAFT * ratio;
        }

        if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.LAVA_CAULDRON)
                || state.isIn(BlockTags.FIRE)
                || state.isIn(BlockTags.CAMPFIRES)
                || state.isOf(Blocks.MAGMA_BLOCK)) {
            return FIRE_UPDRAFT * ratio;
        }

        if (state.isIn(BlockTags.SAND)) {
            return SAND_UPDRAFT * ratio;
        }

        if (state.isIn(BlockTags.SNOW) || state.isIn(BlockTags.ICE)) {
            return ICE_UPDRAFT * ratio;
        }

        return 0;
    }

    private static int getSurfaceBelow(BlockPos.Mutable pos, World world) {
        do {
            pos.move(Direction.DOWN);
        } while (world.isAir(pos) && world.isInBuildLimit(pos));
        return pos.getY();
    }

    public interface TwoDimensionalField {
        float getValue(World world, BlockPos.Mutable pos);

        default Vec3d computedAverage(World world, BlockPos pos) {
            return computeAverage(world, pos, new BlockPos.Mutable());
        }

        default Vec3d computeAverage(World world, BlockPos pos, BlockPos.Mutable probedPosition) {

            float e = getValue(world, probedPosition.set(pos));

            // A(-1,-1) B( 0,-1) C( 1,-1)
            // D(-1, 0) E( 0, 0) F( 1, 0)
            // G(-1, 1) H( 0, 1) I( 1, 1)

            int projectionDistance = 4;

            // DEF
            Vec3d def = new Vec3d(-average(
                    getValue(world, probedPosition.set(pos.getX() - projectionDistance, pos.getY(), pos.getZ())) - e,
                    e - getValue(world, probedPosition.set(pos.getX() + projectionDistance, pos.getY(), pos.getZ()))
            ), 0, 0).normalize();
            // BEH
            Vec3d beh = new Vec3d(0, 0, -average(
                    getValue(world, probedPosition.set(pos.getX(), pos.getY(), pos.getZ() - projectionDistance)) - e,
                    e - getValue(world, probedPosition.set(pos.getX(), pos.getY(), pos.getZ() + projectionDistance))
            )).normalize();

            // AEI
            double diagMag = average(
                    getValue(world, probedPosition.set(pos.getX() - projectionDistance, pos.getY(), pos.getZ() - projectionDistance)) - e,
                    e - getValue(world, probedPosition.set(pos.getX() + projectionDistance, pos.getY(), pos.getZ() + projectionDistance))
            );
            Vec3d aei = new Vec3d(0.5 * diagMag, 0, 0.5 * diagMag).normalize();
            // GEC
            diagMag = average(
                    getValue(world, probedPosition.set(pos.getX() - projectionDistance, pos.getY(), pos.getZ() + projectionDistance)) - e,
                    e - getValue(world, probedPosition.set(pos.getX() + projectionDistance, pos.getY(), pos.getZ() - projectionDistance))
            );
            Vec3d gec = new Vec3d(0.5 * diagMag, 0, 0.5 * diagMag).normalize();

            return beh.add(def).add(aei).add(gec).normalize();
        }

        private static float average(float a, float b) {
            return (a + b) / 2F;
        }
    }
}
