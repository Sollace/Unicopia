package com.minelittlepony.unicopia.core.ability;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.IKeyBindingHandler;
import com.minelittlepony.unicopia.core.Race;
import com.minelittlepony.unicopia.core.entity.player.IPlayer;

import net.minecraft.world.World;

public interface IPower<T extends IPower.IData> extends IKeyBindingHandler.IKeyBinding {

    @Override
    default String getKeyCategory() {
        return "unicopia.category.name";
    }

    /**
     * Returns the number of ticks the player must hold the ability key to trigger this ability.
     */
    int getWarmupTime(IPlayer player);


    /**
     * Returns the number of ticks allowed for cooldown
     */
    int getCooldownTime(IPlayer player);

    /**
     * Called to check preconditions for activating the ability.
     *
     * @param w         The world
     * @param player    The player
     * @return  True to allow activation
     */
    default boolean canActivate(World w, IPlayer player) {
        return true;
    }

    /**
     * Checks if the given race is permitted to use this ability
     * @param playerSpecies The player's species
     */
    boolean canUse(Race playerSpecies);

    /**
     * Called on the client to activate the ability.
     *
     * @param player    The player activating the ability
     * @return  Data to be sent, or null if activation failed
     */
    @Nullable
    T tryActivate(IPlayer player);

    Class<T> getPackageType();

    /**
     * Called to actually apply the ability.
     * Only called on the server side.
     *
     * @param player    The player that triggered the ability
     * @param data      Data previously sent from the client
     */
    void apply(IPlayer player, T data);

    /**
     * Called every tick until the warmup timer runs out.
     * @param player    The current player
     */
    void preApply(IPlayer player);

    /**
     * Called every tick until the cooldown timer runs out.
     * @param player    The current player
     */
    void postApply(IPlayer player);

    public interface IData {

    }
}
