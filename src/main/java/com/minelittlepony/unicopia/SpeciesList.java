package com.minelittlepony.unicopia;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.UConfig;
import com.minelittlepony.unicopia.ducks.IRaceContainerHolder;
import com.minelittlepony.unicopia.entity.IEntity;
import com.minelittlepony.unicopia.entity.player.IPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class SpeciesList {

    private static final SpeciesList instance = new SpeciesList();

    public static SpeciesList instance() {
        return instance;
    }

    public boolean whiteListRace(Race race) {
        boolean result = UConfig.instance().getSpeciesWhiteList().add(race);

        UConfig.instance().save();

        return result;
    }

    public boolean unwhiteListRace(Race race) {
        boolean result = UConfig.instance().getSpeciesWhiteList().remove(race);

        UConfig.instance().save();

        return result;
    }

    public boolean speciesPermitted(Race race, PlayerEntity sender) {
        if (race.isOp() && (sender == null || !sender.abilities.creativeMode)) {
            return false;
        }

        return race.isDefault() || UConfig.instance().getSpeciesWhiteList().isEmpty() || UConfig.instance().getSpeciesWhiteList().contains(race);
    }

    public Race validate(Race race, PlayerEntity sender) {
        if (!speciesPermitted(race, sender)) {
            race = Race.EARTH;

            if (!speciesPermitted(race, sender)) {
                race = Race.HUMAN;
            }
        }

        return race;
    }

    @Nullable
    public IPlayer getPlayer(@Nullable PlayerEntity player) {
        return this.<IPlayer>getEntity(player);
    }

    @Nullable
    public IPlayer getPlayer(UUID playerId) {
        return getPlayer(IPlayer.fromServer(playerId));
    }

    @Nullable
    public <T extends IEntity> T getEntity(Entity entity) {
        return this.<Entity, T>getForEntity(entity)
                .map(IRaceContainerHolder::getRaceContainer)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity, T extends IEntity> Optional<IRaceContainerHolder<T>> getForEntity(Entity entity) {
        if (entity instanceof IRaceContainerHolder) {
            return Optional.of(((IRaceContainerHolder<T>)entity));
        }
        return Optional.empty();
    }
}
