package com.minelittlepony.unicopia.server.world.gen;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Provides a basic event for mods to inject their own biomes in place of vanilla overworld biomes.
 * <p>
 * Mods are able to insert entries for their biomes into the ranges for existing ones by
 * sub-dividing the provided section into pieces and assigning a new biome to one of the create
 * segments.
 */
public interface OverworldBiomeSelectionCallback {
    Event<OverworldBiomeSelectionCallback> EVENT = EventFactory.createArrayBacked(OverworldBiomeSelectionCallback.class, delegates -> {
        return context -> {
            for (OverworldBiomeSelectionCallback delegate : delegates) {
                delegate.onSelectingBiome(context);
            }
        };
    });

    void onSelectingBiome(BiomeSelectionContext context);
}
