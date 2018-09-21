package com.minelittlepony.unicopia.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.minelittlepony.unicopia.Race;

import come.minelittlepony.unicopia.forgebullshit.FBS;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerSpeciesList {

    private static final PlayerSpeciesList instance = new PlayerSpeciesList();

    public static PlayerSpeciesList instance() {
        return instance;
    }

    private List<Race> serverPermittedRaces = new ArrayList<>();

    public boolean speciesPermitted(Race race, EntityPlayer sender) {
        if (race == Race.ALICORN && (sender == null || !sender.capabilities.isCreativeMode)) {
            return false;
        }

        return race.isDefault() || serverPermittedRaces.isEmpty() || serverPermittedRaces.contains(race);
    }

    public IRaceContainer<?> emptyContainer(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return new PlayerCapabilities((EntityPlayer)entity);
        }

        if (entity instanceof EntityItem) {
            return new ItemCapabilities();
        }

        throw new IllegalArgumentException("entity");
    }

    public IPlayer getPlayer(EntityPlayer player) {
        return FBS.of(player).getPlayer();
    }

    public IPlayer getPlayer(UUID playerId) {
        return getPlayer(IPlayer.getPlayerFromServer(playerId));
    }

    public <T extends Entity> IRaceContainer<T> getEntity(T entity) {
        return FBS.of(entity).getRaceContainer();
    }
}
