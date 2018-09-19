package com.minelittlepony.unicopia.advancements;

import com.google.gson.JsonElement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

/**
 * Predicate for testing whether a player has a previous advancement.
 * Allows for unlocking advancements in linear succession.
 */
public class AdvancementPredicate {

    public static AdvancementPredicate deserialize(JsonElement json) {
        return new AdvancementPredicate(json.getAsString());
    }

    private final ResourceLocation id;

    public AdvancementPredicate(String advancement) {
        this.id = new ResourceLocation(advancement);
    }

    public boolean test(WorldServer world, PlayerAdvancements playerAdvancements) {
        Advancement advancement = world.getAdvancementManager().getAdvancement(id);

        return advancement != null && playerAdvancements.getProgress(advancement).isDone();
    }
}
