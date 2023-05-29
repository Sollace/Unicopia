package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.data.Hit;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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

    default int getColor(Pony player) {
        return -1;
    }

    /**
     * Called when an ability is about to be triggered. This event occurs on both the client and server so check {@code Pony#isClient} if you need to know which one you're on.
     * <p>
     * Use this method to respond to quick-time events, like short taps or double-taps.
     * <p>
     * @return True if the event has been handled.
     */
    default boolean onQuickAction(Pony player, ActivationType type, Optional<T> data) {
        return onQuickAction(player, type);
    }

    @Deprecated
    default boolean onQuickAction(Pony player, ActivationType type) {
        return false;
    }

    /**
     * Called on the client to get any data required for the quick action.
     */
    default Optional<T> prepareQuickAction(Pony player, ActivationType type) {
        return Optional.empty();
    }

    /**
     * Called to check preconditions for activating the ability.
     *
     * @param w         The world
     * @param player    The player
     * @return  True to allow activation
     */
    default boolean canActivate(World w, Pony player) {
        return true;
    }

    /**
     * Checks if the given race is permitted to use this ability
     * @param playerSpecies The player's species
     */
    boolean canUse(Race playerSpecies);

    @Deprecated
    @Nullable
    T tryActivate(Pony player);

    /**
     * Called on the client to activate the ability.
     *
     * @param player    The player activating the ability
     * @return  Data to be sent, or null if activation failed
     */
    default Optional<T> prepare(Pony player) {
        return Optional.ofNullable(tryActivate(player));
    }

    Hit.Serializer<T> getSerializer();

    /**
     * The icon representing this ability on the UI and HUD.
     */
    default Identifier getIcon(Pony player) {
        Identifier id = Abilities.REGISTRY.getId(this);
        return new Identifier(id.getNamespace(), "textures/gui/ability/" + id.getPath() + ".png");
    }

    default Text getName(Pony player) {
        return getName();
    }

    /**
     * The display name for this ability.
     */
    default Text getName() {
        return Text.translatable(getTranslationKey());
    }

    default String getTranslationKey() {
        Identifier id = Abilities.REGISTRY.getId(this);
        return "ability." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    /**
     * Server-side counterpart to canActivate.
     *
     * Called before applying to determine whether to cancel the command or not.
     */
    default boolean canApply(Pony player, T data) {
        return true;
    }

    /**
     * Called to actually apply the ability.
     * Only called on the server side.
     *
     * @param player    The player that triggered the ability
     * @param data      Data previously sent from the client
     */
    void apply(Pony player, T data);

    /**
     * Called every tick until the warmup timer runs out.
     * @param player    The current player
     */
    void preApply(Pony player, AbilitySlot slot);

    /**
     * Called every tick until the cooldown timer runs out.
     * @param player    The current player
     */
    void postApply(Pony player, AbilitySlot slot);
}
