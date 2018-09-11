package com.minelittlepony.unicopia.power;

import java.util.Random;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.client.particle.Particles;
import com.minelittlepony.unicopia.input.IKeyBind;
import com.minelittlepony.unicopia.player.IPlayer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
                player.attackEntityFrom(DamageSource.MAGIC, -food);
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

    static void spawnParticles(int particleId, EntityPlayer player, int count) {
        double halfDist = player.getEyeHeight() / 1.5;
        double middle = player.getEntityBoundingBox().minY + halfDist;

        Random rand = player.getEntityWorld().rand;
        for (int i = 0; i < count; i++) {
            double x = (rand.nextFloat() * halfDist) - halfDist;
            double y = (rand.nextFloat() * halfDist) - halfDist;
            double z = (rand.nextFloat() * halfDist) - halfDist;

            Particles.instance().spawnParticle(particleId, false,
                player.posX + x,
                middle + y,
                player.posZ + z,
                0, 0, 0);
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
     * @param w         The player's world
     * @return  Data to be sent, or null if activation failed
     */
    T tryActivate(EntityPlayer player, World w);

    Class<T> getPackageType();

    /**
     * Called to actually apply the ability.
     * Only called on the server side.
     *
     * @param player    The player that triggered the ability
     * @param data      Data previously sent from the client
     */
    @SideOnly(Side.SERVER)
    void apply(EntityPlayer player, T data);

    /**
     * Called just before the ability is activated.
     * @param player    The current player
     */
    @SideOnly(Side.CLIENT)
    void preApply(EntityPlayer player);

    /**
     * Called every tick until the cooldown timer runs out.
     * @param player    The current player
     */
    @SideOnly(Side.CLIENT)
    void postApply(EntityPlayer player);

}
