package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public interface Ability<T extends Hit> {
    /**
     * The amount of energy this ability is expected to cost if the player were to cast it.
     */
    double getCostEstimate(Pony player);

    /**
     * Returns the number of ticks the player must hold the ability key to trigger this ability.
     */
    int getWarmupTime(Pony player);

    /**
     * Returns the number of ticks allowed for cooldown
     */
    int getCooldownTime(Pony player);

    /**
     * The icon representing this ability on the UI and HUD.
     */
    default Identifier getIcon(Pony player) {
        return getId().withPath(p -> "textures/gui/ability/" + p + ".png");
    }

    default int getColor(Pony player) {
        return -1;
    }

    /**
     * The display name for this ability.
     */
    default Text getName(Pony player) {
        return Text.translatable(getTranslationKey());
    }

    default String getTranslationKey() {
        return Util.createTranslationKey("ability", getId());
    }

    default Identifier getId() {
        return Abilities.REGISTRY.getId(this);
    }

    default boolean activateOnEarlyRelease() {
        return false;
    }

    /**
     * Checks if the given race is permitted to use this ability
     * @param race The player's species
     */
    default boolean canUse(Race.Composite race) {
        return race.any(this::canUse);
    }

    /**
     * Checks if the given race is permitted to use this ability
     * @param playerSpecies The player's species
     */
    boolean canUse(Race playerSpecies);

    /**
     * Called when an ability is about to be triggered. This event occurs on both the client and server so check {@code Pony#isClient} if you need to know which one you're on.
     * <p>
     * Use this method to respond to quick-time events, like short taps or double-taps.
     * <p>
     * @return True if the event has been handled.
     */
    default boolean onQuickAction(Pony player, ActivationType type, Optional<T> data) {
        return false;
    }

    /**
     * Called on the client to get any data required for the quick action.
     *
     * @param player    The player
     * @param type      The type of quick event being triggered
     * @return The data to pass on to the quick event handler
     */
    default Optional<T> prepareQuickAction(Pony player, ActivationType type) {
        return Optional.empty();
    }

    /**
     * Gets the serializer to use for reading data over the network.
     */
    Hit.Serializer<T> getSerializer();

    /**
     * Called on the client to get any data required to activate the ability.
     *
     * @param player    The player activating the ability
     * @return  Data to be sent, or Empty if activation failed
     */
    Optional<T> prepare(Pony player);

    /**
     * Called to actually apply the ability.
     * Only called on the server side.
     *
     * @param player    The player that triggered the ability
     * @param data      Data previously sent from the client
     * @return True if the ability succeeded. Returning false will cause an ability reset message to be sent to the client.
     */
    boolean apply(Pony player, T data);

    /**
     * Called every tick until the warmup timer runs out.
     * @param player    The current player
     */
    void warmUp(Pony player, AbilitySlot slot);

    /**
     * Called every tick until the cooldown timer runs out.
     * @param player    The current player
     */
    void coolDown(Pony player, AbilitySlot slot);
}
