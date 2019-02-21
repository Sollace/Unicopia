package com.minelittlepony.unicopia.power;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.input.IKeyBind;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IPower<T extends IData> extends IKeyBind {

    /**
     * Subtracts a given food amount from the player.
     * Harms the player if there is not enough enough hunger available.
     */
    static boolean takeFromPlayer(EntityPlayer player, double foodSubtract) {
        if (!player.capabilities.isCreativeMode) {
            int food = (int)(player.getFoodStats().getFoodLevel() - foodSubtract);
            if (food < 0) {
                player.getFoodStats().addStats(-player.getFoodStats().getFoodLevel(), 0);
                player.attackEntityFrom(DamageSource.MAGIC, -food/2);
            } else {
                player.getFoodStats().addStats((int)-foodSubtract, 0);
            }
        }

        return player.getHealth() > 0;
    }

    static double getPlayerEyeYPos(EntityPlayer player) {
        if (player.getEntityWorld().isRemote) {
            return player.posY + player.getEyeHeight() - player.getYOffset();
        }
        return player.posY + player.getEyeHeight() - 1;
    }

    static void spawnParticles(int particleId, Entity entity, int count, int...args) {
        double halfDist = entity.getEyeHeight() / 1.5;
        double middle = entity.getEntityBoundingBox().minY + halfDist;

        IShape shape = new Sphere(false, (float)halfDist + entity.width);

        for (int i = 0; i < count; i++) {
            Vec3d point = shape.computePoint(entity.getEntityWorld().rand);

            Particles.instance().spawnParticle(particleId, false,
                    entity.posX + point.x,
                    middle + point.y,
                    entity.posZ + point.z,
                    0, 0, 0, args);
        }
    }

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

}
