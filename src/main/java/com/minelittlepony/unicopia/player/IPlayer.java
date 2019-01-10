package com.minelittlepony.unicopia.player;

import java.util.UUID;

import com.minelittlepony.model.anim.IInterpolator;
import com.minelittlepony.unicopia.enchanting.IPageOwner;
import com.minelittlepony.unicopia.network.ITransmittable;
import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface IPlayer extends ICaster<EntityPlayer>, IRaceContainer<EntityPlayer>, ITransmittable, IPageOwner {

    IAbilityReceiver getAbilities();

    IGravity getGravity();

    IView getCamera();

    IInterpolator getInterpolator();

    float getExertion();

    void setExertion(float exertion);

    default void addExertion(int exertion) {
        setExertion(getExertion() + exertion/100F);
    }

    boolean isClientPlayer();

    void copyFrom(IPlayer oldPlayer);

    void onEntityEat();

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

    static EntityPlayer getPlayerFromClient(UUID playerId) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player.getUniqueID().equals(playerId)) {
            return mc.player;
        }

        return mc.world.getPlayerEntityByUUID(playerId);
    }
}
