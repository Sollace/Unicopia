package com.minelittlepony.unicopia.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.minelittlepony.unicopia.Race;

import come.minelittlepony.unicopia.forgebullshit.FBS;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerSpeciesList {

    private static final PlayerSpeciesList instance = new PlayerSpeciesList();

    public static PlayerSpeciesList instance() {
        return instance;
    }

    private List<Race> serverPermittedRaces = new ArrayList<>();

    public boolean speciesPermitted(Race race) {
        return race.isDefault() || serverPermittedRaces.isEmpty() || serverPermittedRaces.contains(race);
    }

    public IPlayer emptyPlayer(EntityPlayer player) {
        return new PlayerCapabilities(player);
    }

    public IPlayer getPlayer(EntityPlayer player) {
        return FBS.of(player).getPlayer();
    }

    public IPlayer getPlayer(UUID playerId) {
        return getPlayer(IPlayer.getPlayerEntity(playerId));
    }
}
