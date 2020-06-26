package com.minelittlepony.unicopia.world.advancement;

import com.google.gson.JsonElement;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Predicate for testing whether a player has a previous advancement.
 * Allows for unlocking advancements in linear succession.
 */
class AdvancementPredicate {

    public static AdvancementPredicate deserialize(JsonElement json) {
        return new AdvancementPredicate(json.getAsString());
    }

    private final Identifier id;

    public AdvancementPredicate(String advancement) {
        this.id = new Identifier(advancement);
    }

    public boolean test(ServerWorld world, PlayerAdvancementTracker tracker) {
        Advancement advancement = world.getServer().getAdvancementLoader().get(id);

        return advancement != null && tracker.getProgress(advancement).isDone();
    }
}
