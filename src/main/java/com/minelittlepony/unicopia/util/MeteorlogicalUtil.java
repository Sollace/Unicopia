package com.minelittlepony.unicopia.util;

import java.util.Locale;

import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.server.world.UnicopiaWorldProperties;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.LunarWorldView;
import net.minecraft.world.World;

public interface MeteorlogicalUtil {
    float SUNRISE = 0;
    float NOON = 0.5F;
    float SUNSET = 1;
    float MIDNIGHT = 1.5F;
    float MOONSET = 2;

    /**
     * @see net.minecraft.world.dimension.DimensionType#MOON_SIZES
     */
    int FULL_MOON = 0;

    static boolean isBetween(float skyAngle, float start, float end) {
        return skyAngle >= start && skyAngle <= end;
    }

    static boolean isCloseTo(float skyAngle, float value, float deviation) {
        return isBetween(skyAngle, value - deviation, value + deviation);
    }

    static boolean isLookingIntoSun(World world, Entity entity) {

        // check first whether the world has a sun
        if (!world.getDimension().hasSkyLight()) {
            return false;
        }

        // check if sun is obscured by clouds
        if (world.hasRain(entity.getBlockPos()) || world.isThundering()) {
            return false;
        }

        final float skyAngle = getSkyAngle(entity.getWorld());
        float playerYaw = MathHelper.wrapDegrees(entity.getHeadYaw());
        float playerAngle = (-entity.getPitch(1) / 90F) / 2F;

        // player is facing the other way so flip the yaw to match sun angle
        if (playerYaw > 0) {
            playerAngle = 1 - playerAngle;
        }

        if (world.isClient()) {
            playerYaw += UnicopiaClient.getInstance().tangentalSkyAngle.getValue();
        } else {
            playerYaw += UnicopiaWorldProperties.forWorld((ServerWorld)world).getTangentalSkyAngle();
        }

        playerYaw = Math.abs(playerYaw);

        // check if day,
        // and player is looking towards the sun, and that there isn't a block obstructing their view
        return skyAngle < 1
            && (playerYaw > 89 && playerYaw < 92 || (playerAngle > 0.45F && playerAngle < 0.55F))
            && playerAngle > (skyAngle - 0.04F) && playerAngle < (skyAngle + 0.04F)
            && entity.raycast(100, 1, true).getType() == Type.MISS;
    }

    static float getSunIntensity(World world) {
        float skyAngle = getSkyAngle(world);
        if (skyAngle > SUNSET) {
            return 0;
        }

        // intensity (0-1) has a peak at 0.5 (midday)
        float intensity = MathHelper.cos((skyAngle - 0.5F) * MathHelper.PI);

        if (world.isRaining()) {
            intensity *= 0.5;
        }
        if (world.isThundering()) {
            intensity *= 0.5;
        }

        return intensity;
    }

    /**
     * Gets the sun angle on a scale of 0-2 where 0=sunrise, 1=sunset, and >1 is night
     *
     * 0 = sunrise
     * 0-0.5 = morning
     * 0.5 = noon
     * 0.5-1 = evening
     * 1 = sunset
     * 1-1.5 = night
     * 1.5 = midnight
     * 1.5-0 = night
     *
     * @param world
     * @return The sun angle
     */
    static float getSkyAngle(LunarWorldView world) {
        return ((world.getSkyAngle(1) + 0.25F) % 1F) * 2;
    }

    /**
     * Gets the current day phase.
     */
    static DayPhase getDayPhase(LunarWorldView world) {
        float skyAngle = getSkyAngle(world);

        if (isCloseTo(skyAngle, SUNRISE, 0.1F)) {
            return DayPhase.SUNRISE;
        }
        if (skyAngle < 0.5F) {
            return DayPhase.MORNING;
        }
        if (isCloseTo(skyAngle, NOON, 0.1F)) {
            return DayPhase.NOON;
        }
        if (skyAngle < 1F) {
            return DayPhase.EVENING;
        }
        if (isCloseTo(skyAngle, SUNSET, 0.1F)) {
            return DayPhase.SUNSET;
        }
        if (skyAngle < MIDNIGHT) {
            return DayPhase.NIGHT;
        }
        if (isCloseTo(skyAngle, MIDNIGHT, 0.1F)) {
            return DayPhase.MIDNIGHT;
        }
        return DayPhase.AFTER_MIDNIGHT;
    }

    static boolean isPositionExposedToSun(World world, BlockPos pos) {
        if (world.isClient) {
            world.calculateAmbientDarkness();
        }

        return world.getDimension().hasSkyLight()
                && world.getLightLevel(LightType.SKY, pos) >= 12
                && !world.isRaining()
                && !world.isThundering()
                && world.isDay();
    }

    public enum DayPhase implements StringIdentifiable {
        SUNRISE(true),
        MORNING(true),
        NOON(true),
        EVENING(true),
        SUNSET(false),
        NIGHT(false),
        MIDNIGHT(false),
        AFTER_MIDNIGHT(false);

        public static final DayPhase[] VALUES = values();

        public static final Codec<DayPhase> CODEC = StringIdentifiable.createCodec(DayPhase::values);
        public static final PacketCodec<ByteBuf, DayPhase> PACKET_CODEC = PacketCodecs.indexed(i -> VALUES[i], DayPhase::ordinal);

        private final boolean day;
        private final boolean transitionary;

        private final String name = name().toLowerCase(Locale.ROOT);

        DayPhase(boolean day) {
            this.day = day;
            this.transitionary = ordinal() % 2 == 0;
        }

        public boolean isDay() {
            return day;
        }

        public boolean isTransition() {
            return transitionary;
        }

        public DayPhase simplified() {
            if (this == SUNRISE) {
                return MORNING;
            }
            if (this == NOON || this == SUNSET) {
                return EVENING;
            }
            if (this == MIDNIGHT || this == AFTER_MIDNIGHT) {
                return NIGHT;
            }
            return this;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}
