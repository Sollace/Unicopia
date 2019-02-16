package com.minelittlepony.unicopia.player;

import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.model.anim.IInterpolator;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.enchanting.IPageOwner;
import com.minelittlepony.unicopia.network.ITransmittable;
import com.minelittlepony.unicopia.spell.ICaster;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * The player.
 *
 * This is the core of unicopia.
 */
public interface IPlayer extends ICaster<EntityPlayer>, IRaceContainer<EntityPlayer>, ITransmittable, IPageOwner {

    /**
     * Gets the player's magical abilities delegate responsible for all spell casting and persisting/updating.
     */
    IAbilityReceiver getAbilities();

    /**
     * Gets the gravity delegate responsible for updating flight states
     */
    IGravity getGravity();

    /**
     * Gets the player's viewport.
     */
    IView getCamera();

    /**
     * Gets the food delegate to handle player eating.
     */
    IFood getFood();

    /**
     * Gets an animation interpolator.
     */
    IInterpolator getInterpolator();

    /**
     * Gets the amount of exertion this player has put toward any given activity.
     * This is simillar to tiredness.
     */
    float getExertion();

    /**
     * Sets the player's exertion level.
     */
    void setExertion(float exertion);

    /**
     * Adds player tiredness.
     */
    default void addExertion(int exertion) {
        setExertion(getExertion() + exertion/100F);
    }

    /**
     * Gets the amount of excess energy the player has.
     * This is increased by eating sugar.
     */
    float getEnergy();

    /**
     * Sets the player's energy level.
     */
    void setEnergy(float energy);

    /**
     * Adds energy to the player's existing energy level.
     */
    default void addEnergy(int energy) {
        setEnergy(getEnergy() + energy / 100F);
    }

    /**
     * Returns true if this player is fully invisible.
     * Held items and other effects will be hidden as well.
     */
    boolean isInvisible();

    /**
     * Sets whether this player should be invisible.
     */
    void setInvisible(boolean invisible);

    void copyFrom(IPlayer oldPlayer);

    /**
     * Called when the player steps on clouds.
     */
    boolean stepOnCloud();

    /**
     * Called when this player finishes eating food.
     */
    void onEat(ItemStack stack, @Nullable ItemFood food);

    /**
     * Called when this player falls.
     */
    void onFall(float distance, float damageMultiplier);

    /**
     * Event triggered when this player is hit by a projectile.
     * @param projectile The projectile doing the hitting.
     *
     * @return True if the hit was successful.
     */
    boolean onProjectileImpact(Entity projectile);

    /**
     * Returns true if this player is the use.
     */
    default boolean isClientPlayer() {
        return UClient.instance().isClientPlayer(getOwner());
    }

    static EntityPlayer getPlayerFromServer(UUID playerId) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (server == null) {
            return UClient.instance().getPlayerByUUID(playerId);
        }

        Entity e = server.getEntityFromUuid(playerId);

        if (e instanceof EntityPlayer) {
            return (EntityPlayer)e;
        }

        return null;
    }

    static boolean equal(GameProfile one, GameProfile two) {
        return one == two || (one != null && two != null && one.getId().equals(two.getId()));
    }

    static boolean equal(EntityPlayer one, EntityPlayer two) {
        return one == two || (one != null && two != null && equal(one.getGameProfile(), two.getGameProfile()));
    }
}
