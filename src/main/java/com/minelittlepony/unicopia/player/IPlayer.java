package com.minelittlepony.unicopia.player;

import java.util.UUID;

import com.minelittlepony.unicopia.InbtSerialisable;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public interface IPlayer extends ICaster<EntityPlayer>, InbtSerialisable {
    Race getPlayerSpecies();

    void setPlayerSpecies(Race race);

    void sendCapabilities();

    IAbilityReceiver getAbilities();

    boolean isClientPlayer();

    void onEntityUpdate();

    default void onEntityEat() {

    }

    static EntityPlayer getPlayerEntity(UUID playerId) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(playerId);
    }
}
