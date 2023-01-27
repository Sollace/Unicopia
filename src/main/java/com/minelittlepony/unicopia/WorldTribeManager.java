package com.minelittlepony.unicopia;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class WorldTribeManager extends PersistentState {

    private Race defaultRace = Race.UNSET;

    public WorldTribeManager() {}

    public WorldTribeManager(NbtCompound nbt) {
        defaultRace = Race.fromName(nbt.getString("defaultRace"), Race.HUMAN);
    }

    public Race getDefaultRace() {
        return defaultRace;
    }

    public Race setDefaultRace(Race race) {
        defaultRace = race;
        markDirty();
        return defaultRace;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("defaultRace", Race.REGISTRY.getId(defaultRace).toString());
        return tag;
    }

    public static WorldTribeManager forWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(WorldTribeManager::new, WorldTribeManager::new, "unicopia:tribes");
    }
}
