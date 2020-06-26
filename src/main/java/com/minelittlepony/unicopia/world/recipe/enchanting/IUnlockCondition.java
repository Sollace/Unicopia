package com.minelittlepony.unicopia.world.recipe.enchanting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * A PageEvent for determining when certain pages must be unlocked.
 */
public interface IUnlockCondition<T extends IUnlockEvent> {

    /**
     * Returns true if event instanceof T
     */
    default boolean accepts(IUnlockEvent event) {
        return true;
    }

    /**
     * Checks if this event's conditions are met.
     * @param prop      PlayerExtension for the player doing the crafting
     * @param stack     ItemStack crafted
     */
    boolean matches(PageOwner owner, T event);

    default void require(JsonObject json, String memberName) {
        if (!json.has(memberName)) {
            throw new JsonParseException(String.format("%s condition must have a %s", getClass().getSimpleName(), memberName));
        }
    }
}