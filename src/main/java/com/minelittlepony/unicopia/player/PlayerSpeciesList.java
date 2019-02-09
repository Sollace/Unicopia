package com.minelittlepony.unicopia.player;

import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UConfig;
import com.minelittlepony.unicopia.forgebullshit.FBS;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerSpeciesList {

    private static final PlayerSpeciesList instance = new PlayerSpeciesList();

    public static PlayerSpeciesList instance() {
        return instance;
    }

    public boolean whiteListRace(Race race) {
        boolean result = UConfig.getInstance().getSpeciesWhiteList().add(race);

        UConfig.getInstance().save();

        return result;
    }

    public boolean unwhiteListRace(Race race) {
        boolean result = UConfig.getInstance().getSpeciesWhiteList().remove(race);

        UConfig.getInstance().save();

        return result;
    }

    public boolean speciesPermitted(Race race, EntityPlayer sender) {
        if (race.isOp() && (sender == null || !sender.capabilities.isCreativeMode)) {
            return false;
        }

        return race.isDefault() || UConfig.getInstance().getSpeciesWhiteList().isEmpty() || UConfig.getInstance().getSpeciesWhiteList().contains(race);
    }

    public Race validate(Race race, EntityPlayer sender) {
        if (!speciesPermitted(race, sender)) {
            race = Race.EARTH;

            if (!speciesPermitted(race, sender)) {
                race = Race.HUMAN;
            }
        }

        return race;
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

    @Nullable
    public IPlayer getPlayer(EntityPlayer player) {
        if (player == null) {
            return null;
        }

        return FBS.of(player).getPlayer();
    }

    @Nullable
    public IPlayer getPlayer(UUID playerId) {
        return getPlayer(IPlayer.getPlayerFromServer(playerId));
    }

    public <T extends Entity> IRaceContainer<T> getEntity(T entity) {
        return FBS.of(entity).getRaceContainer();
    }
}
