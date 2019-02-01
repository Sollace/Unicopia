package com.minelittlepony.unicopia.player;

import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.model.anim.IInterpolator;
import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.enchanting.IPageOwner;
import com.minelittlepony.unicopia.network.ITransmittable;
import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface IPlayer extends ICaster<EntityPlayer>, IRaceContainer<EntityPlayer>, ITransmittable, IPageOwner {

    IAbilityReceiver getAbilities();

    IGravity getGravity();

    IView getCamera();

    IFood getFood();

    IInterpolator getInterpolator();

    float getExertion();

    void setExertion(float exertion);

    default void addExertion(int exertion) {
        setExertion(getExertion() + exertion/100F);
    }

    float getEnergy();

    void setEnergy(float energy);

    default void addEnergy(int energy) {
        setEnergy(getEnergy() + energy / 100F);
    }

    default boolean isClientPlayer() {
        return UClient.instance().isClientPlayer(getOwner());
    }

    void copyFrom(IPlayer oldPlayer);

    void onEat(ItemStack stack, @Nullable ItemFood food);

    boolean stepOnCloud();

    void onFall(float distance, float damageMultiplier);

    void beforeUpdate(EntityPlayer entity);

    static EntityPlayer getPlayerFromServer(UUID playerId) {
        Entity e = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(playerId);

        if (e instanceof EntityPlayer) {
            return (EntityPlayer)e;
        }

        return null;
    }
}
