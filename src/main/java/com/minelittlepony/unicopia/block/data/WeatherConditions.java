package com.minelittlepony.unicopia.block.data;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class WeatherConditions extends PersistentState implements Tickable {
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
    public static final float MAX_WIND_HEIGHT = 70;

    private static final Identifier ID = Unicopia.id("weather_conditions");

    public static WeatherConditions get(World world) {
        return WorldOverlay.getPersistableStorage(world, ID, WeatherConditions::new, WeatherConditions::new);
    }

    private final World world;

    private float windYaw;
    private float prevWindYaw;
    private int interpolation;
    private int maxInterpolation = 100;

    private boolean prevDayState;

    private WeatherConditions(World world, NbtCompound compound) {
        this(world);
        windYaw = compound.getFloat("windYaw");
        prevWindYaw = compound.getFloat("prevWindYaw");
        prevDayState = compound.getBoolean("prevDayState");
        interpolation = compound.getInt("interpolation");
        maxInterpolation = compound.getInt("maxInterpolation");
    }

    private WeatherConditions(World world) {
        this.world = world;
    }

    @Override
    public void tick() {
        if (interpolation < maxInterpolation) {
            interpolation++;
            markDirty();
        }

        boolean isDay = world.isDay();
        if (isDay != prevDayState
            || world.random.nextInt(1200) == 0
            || (world.isRaining() && world.random.nextInt(120) == 0)
            || (world.isThundering() && world.random.nextInt(90) == 0)) {
            prevDayState = isDay;
            prevWindYaw = getWindYaw();
            windYaw = world.random.nextFloat() * 360;
            interpolation = 0;
            maxInterpolation = world.isRaining() || world.isThundering() ? 50 : 100;
            markDirty();
        }
    }

    public float getWindYaw() {
        return MathHelper.lerp(interpolation / (float)maxInterpolation, prevWindYaw, windYaw);
    }

    public Vec3d getWindDirection() {
        return Vec3d.fromPolar(0, windYaw).normalize();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        compound.putFloat("windYaw", windYaw);
        compound.putFloat("prevWindYaw", prevWindYaw);
        compound.putBoolean("prevDayState", prevDayState);
        compound.putInt("interpolation", interpolation);
        compound.putInt("maxInterpolation", maxInterpolation);
        return compound;
    }

    public static Vec3d getAirflow(BlockPos pos, World world) {
        BlockPos.Mutable probedPosition = new BlockPos.Mutable();

        final float localAltitude = LOCAL_ALTITUDE_FIELD.getValue(world, probedPosition.set(pos));
        final float terrainFactor = Math.min(MAX_TERRAIN_HEIGHT, localAltitude) / MAX_TERRAIN_HEIGHT;
        final float windFactor = Math.min(MAX_WIND_HEIGHT, localAltitude) / MAX_WIND_HEIGHT;

        Vec3d terrainGradient = LOCAL_ALTITUDE_FIELD.computeAverage(world, pos, probedPosition).multiply(1 - terrainFactor);
        Vec3d thermalGradient = THERMAL_FIELD.computeAverage(world, pos, probedPosition).multiply(terrainFactor);
        Vec3d wind = get(world).getWindDirection().multiply(1 - windFactor);

        return terrainGradient.add(thermalGradient).add(wind).normalize().add(0, getUpdraft(probedPosition.set(pos), world), 0);
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
