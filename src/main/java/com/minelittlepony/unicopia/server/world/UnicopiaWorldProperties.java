package com.minelittlepony.unicopia.server.world;

import java.util.HashSet;
import java.util.Set;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSkyAngle;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class UnicopiaWorldProperties extends PersistentState {
    static final Codec<Set<BlockPos>> POS_CODEC = CodecUtils.setOf(BlockPos.CODEC);
    public static final PersistentStateType<UnicopiaWorldProperties> TYPE = new PersistentStateType<>(
            "unicopia_tribes",
            UnicopiaWorldProperties::new,
            context -> RecordCodecBuilder.create(i -> i.group(
                    Race.CODEC.fieldOf("defaultRace").forGetter(o -> o.defaultRace),
                    Codec.FLOAT.fieldOf("tangentalSkyAngle").forGetter(o -> o.tangentalSkyAngle),
                    POS_CODEC.fieldOf("activeAltars").forGetter(o -> o.activeAltarPositions)
            ).apply(i, (defaultRace, tangentalSkyAngle, activeAltars) -> new UnicopiaWorldProperties(context, defaultRace, tangentalSkyAngle, activeAltars))),
            DataFixTypes.LEVEL
    );

    private final ServerWorld world;

    private Race defaultRace = Race.UNSET;
    private float tangentalSkyAngle;

    private final Set<BlockPos> activeAltarPositions = new HashSet<>();

    public static UnicopiaWorldProperties forWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    private UnicopiaWorldProperties(Context context) {
        this.world = context.getWorldOrThrow();
    }

    private UnicopiaWorldProperties(Context context, Race defaultRace, float tangentalSkyAngle, Set<BlockPos> activeAltars) {
        this(context);
        this.defaultRace = defaultRace;
        this.tangentalSkyAngle = tangentalSkyAngle;
        this.activeAltarPositions.addAll(activeAltars);
    }

    public Race getDefaultRace() {
        return defaultRace;
    }

    public Race setDefaultRace(Race race) {
        defaultRace = race;
        markDirty();
        return defaultRace;
    }

    public float getTangentalSkyAngle() {
        return tangentalSkyAngle;
    }

    public void setTangentalSkyAngle(float angle) {
        tangentalSkyAngle = MathHelper.wrapDegrees(angle);
        markDirty();
        Channel.SERVER_SKY_ANGLE.sendToAllPlayers(new MsgSkyAngle(tangentalSkyAngle), world);
    }

    public void removeAltar(BlockPos center) {
        activeAltarPositions.remove(center);
        markDirty();
    }

    public void addAltar(BlockPos center) {
        activeAltarPositions.add(center);
        markDirty();
    }

    public boolean isActiveAltar(BlockPos center) {
        return activeAltarPositions.contains(center);
    }

    public boolean isActiveAltar(Entity entity) {
        for (int i = 0; i < entity.getHeight(); i++) {
            if (isActiveAltar(entity.getBlockPos().up(i))) {
                return true;
            }
        }
        return false;
    }
}
