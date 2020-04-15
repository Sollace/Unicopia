package com.minelittlepony.unicopia.ability;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.minelittlepony.unicopia.IKeyBinding;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Ability<T extends Ability.IData> extends IKeyBinding {

    @Override
    default String getKeyCategory() {
        return "unicopia.category.name";
    }

    /**
     * Returns the number of ticks the player must hold the ability key to trigger this ability.
     */
    int getWarmupTime(Pony player);

    /**
     * Returns the number of ticks allowed for cooldown
     */
    int getCooldownTime(Pony player);

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

    /**
     * Called on the client to activate the ability.
     *
     * @param player    The player activating the ability
     * @return  Data to be sent, or null if activation failed
     */
    @Nullable
    T tryActivate(Pony player);

    Class<T> getPackageType();

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
    void preApply(Pony player);

    /**
     * Called every tick until the cooldown timer runs out.
     * @param player    The current player
     */
    void postApply(Pony player);

    public interface IData {

    }

    class Pos implements Ability.IData {
        @Expose
        public int x;

        @Expose
        public int y;

        @Expose
        public int z;

        public Pos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Pos(BlockPos pos) {
            x = pos.getX();
            y = pos.getY();
            z = pos.getZ();
        }

        public BlockPos pos() {
            return new BlockPos(x, y, z);
        }
    }

    class Hit implements Ability.IData {

    }

    class Numeric implements IData {
        @Expose
        public int type;

        public Numeric(int t) {
            type = t;
        }
    }
}
