package com.minelittlepony.unicopia.player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.minelittlepony.jumpingcastle.api.Target;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.network.MsgPlayerCapabilities;

import come.minelittlepony.unicopia.forgebullshit.FBS;
import come.minelittlepony.unicopia.forgebullshit.IPlayerCapabilitiesProxyContainer;
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

    public void sendCapabilities(UUID playerId) {
        Unicopia.channel.send(new MsgPlayerCapabilities(getPlayer(playerId).getPlayerSpecies(), playerId), Target.SERVER_AND_CLIENTS);
    }

    public void handleSpeciesChange(UUID playerId, Race race) {
        getPlayer(playerId).setPlayerSpecies(race);
    }

    public IPlayer emptyPlayer(UUID playerId) {
        return new PlayerCapabilities(playerId);
    }

    public IPlayer getPlayer(EntityPlayer player) {
        if (player == null) {
            return DefaultPlayerSpecies.INSTANCE;
        }

        IPlayerCapabilitiesProxyContainer container = FBS.of(player);

        IPlayer ply = container.getPlayer();
        if (ply == null) {
            ply = emptyPlayer(player.getGameProfile().getId());

            container.setPlayer(ply);
        }

        return ply;
    }

    public IPlayer getPlayer(UUID playerId) {
        return getPlayer(IPlayer.getPlayerEntity(playerId));
    }
}
