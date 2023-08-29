package com.minelittlepony.unicopia.server.world;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgSkyAngle;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;

public class UnicopiaWorldProperties extends PersistentState {

    private final ServerWorld world;

    private Race defaultRace = Race.UNSET;
    private float tangentalSkyAngle;

    public UnicopiaWorldProperties(ServerWorld world) {
        this.world = world;
    }

    public UnicopiaWorldProperties(ServerWorld world, NbtCompound tag) {
        this(world);
        defaultRace = Race.fromName(tag.getString("defaultRace"), Race.HUMAN);
        tangentalSkyAngle = tag.getFloat("tangentalSkyAngle");
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

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("defaultRace", Race.REGISTRY.getId(defaultRace).toString());
        tag.putFloat("tangentalSkyAngle", tangentalSkyAngle);
        return tag;
    }

    public static UnicopiaWorldProperties forWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                nbt -> new UnicopiaWorldProperties(world, nbt),
                () -> new UnicopiaWorldProperties(world), "unicopia_tribes"
        );
    }
}
