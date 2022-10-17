package com.minelittlepony.unicopia;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.PersistentState;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

public class WorldTribeManager extends PersistentState {

    private Race defaultRace = Race.HUMAN;

    public WorldTribeManager() {}

    public WorldTribeManager(NbtCompound nbt) {
        defaultRace = Race.fromName(nbt.getString("defaultRace"), Race.HUMAN);
    }

    public Race getDefaultRace() {
        return defaultRace;
    }

    public Race setDefaultRace(Race race) {
        return defaultRace = race;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("defaultRace", Race.REGISTRY.getId(defaultRace).toString());
        return tag;
    }

    public static String nameFor(RegistryEntry<DimensionType> dimension) {
        if (dimension.matchesKey(DimensionTypes.THE_END)) {
            return "unicopia:tribes_end";
        }
        return "unicopia:tribes";
    }

    public static WorldTribeManager forWorld(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(WorldTribeManager::new, WorldTribeManager::new, nameFor(world.getDimensionEntry()));
    }
}
