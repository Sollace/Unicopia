package com.minelittlepony.unicopia;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;

public class WorldTribeManager extends PersistentState {

    private Race defaultRace = Unicopia.getConfig().getPrefferedRace();

    public WorldTribeManager(ServerWorld world) {
        super(nameFor(world.getDimension()));
    }

    public Race getDefaultRace() {
        return defaultRace;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        defaultRace = Race.fromName(tag.getString("defaultRace"));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putString("defaultRace", defaultRace.name());
        return tag;
    }

    public static String nameFor(DimensionType dimension) {
        return "unicopia:tribes" + dimension.getSuffix();
    }

    public static WorldTribeManager forWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new WorldTribeManager(world), nameFor(world.getDimension()));
    }
}
