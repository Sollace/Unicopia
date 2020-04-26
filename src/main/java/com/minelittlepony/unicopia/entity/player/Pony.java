package com.minelittlepony.unicopia.entity.player;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.enchanting.PageOwner;
import com.minelittlepony.unicopia.entity.FlightControl;
import com.minelittlepony.unicopia.entity.Ponylike;
import com.minelittlepony.unicopia.entity.RaceContainer;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.network.Transmittable;
import com.minelittlepony.util.IInterpolator;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;

/**
 * The player.
 *
 * This is the core of unicopia.
 */
public interface Pony extends Caster<PlayerEntity>, RaceContainer<PlayerEntity>, Transmittable {

    /**
     * Gets the player's magical abilities delegate responsible for all spell casting and persisting/updating.
     */
    AbilityDispatcher getAbilities();

    /**
     * Gets the gravity delegate responsible for updating flight states
     */
    GravityDelegate getGravity();

    /**
     * Gets the flight delegate.
     */
    FlightControl getFlight();

    /**
     * Gets the player's viewport.
     */
    PlayerCamera getCamera();

    MagicReserves getMagicalReserves();

    /**
     * Gets the inventory delegate for this player.
     */
    PlayerInventory getInventory();

    /**
     * Gets an animation interpolator.
     */
    IInterpolator getInterpolator();

    PageOwner getPages();

    /**
     */
    float getExtendedReach();

    void copyFrom(Pony oldPlayer);

    /**
     * Called when the player steps on clouds.
     */
    boolean stepOnCloud();

    /**
     * Called when this player falls.
     */
    float onImpact(float distance);

    /**
     * Attempts to sleep in a bed.
     *
     * @param pos The position of the bed
     *
     * @return The sleep result.
     */
    Either<PlayerEntity.SleepFailureReason, Unit> trySleep(BlockPos pos);

    /**
     * Returns true if this player is the use.
     */
    default boolean isClientPlayer() {
        return InteractionManager.instance().isClientPlayer(getOwner());
    }

    @Nullable
    static Pony of(@Nullable PlayerEntity player) {
        return Ponylike.<Pony>of(player);
    }

    static boolean equal(GameProfile one, GameProfile two) {
        return one == two || (one != null && two != null && one.getId().equals(two.getId()));
    }

    static boolean equal(PlayerEntity one, PlayerEntity two) {
        return one == two || (one != null && two != null && equal(one.getGameProfile(), two.getGameProfile()));
    }
}
