package com.minelittlepony.unicopia.player;

import java.util.UUID;

import com.minelittlepony.unicopia.InbtSerialisable;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface IPlayer extends ICaster<EntityPlayer>, InbtSerialisable, IUpdatable {
    Race getPlayerSpecies();

    void setPlayerSpecies(Race race);

    void sendCapabilities(boolean full);

    IAbilityReceiver getAbilities();

    boolean isClientPlayer();

    void copyFrom(IPlayer oldPlayer);

    default void onEntityEat() {

    }

    default void onFall(float distance, float damageMultiplier) {

    }

    static EntityPlayer getPlayerEntity(UUID playerId) {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerId);

        if (player == null) {
            Entity e = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityFromUuid(playerId);
            if (e instanceof EntityPlayer) {
                return (EntityPlayer)e;
            }
        }

        return player;
    }
}
