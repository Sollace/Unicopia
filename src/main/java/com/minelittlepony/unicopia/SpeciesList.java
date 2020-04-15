package com.minelittlepony.unicopia;

import java.util.Optional;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Config;
import com.minelittlepony.unicopia.ducks.RaceContainerHolder;
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
        boolean result = Config.instance().getSpeciesWhiteList().add(race);

        Config.instance().save();

        return result;
    }

    public boolean unwhiteListRace(Race race) {
        boolean result = Config.instance().getSpeciesWhiteList().remove(race);

        Config.instance().save();

        return result;
    }

    public boolean speciesPermitted(Race race, PlayerEntity sender) {
        if (race.isOp() && (sender == null || !sender.abilities.creativeMode)) {
            return false;
        }

        return race.isDefault() || Config.instance().getSpeciesWhiteList().isEmpty() || Config.instance().getSpeciesWhiteList().contains(race);
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
    public <T extends IEntity> T getEntity(Entity entity) {
        return this.<Entity, T>getForEntity(entity)
                .map(RaceContainerHolder::getRaceContainer)
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity, T extends IEntity> Optional<RaceContainerHolder<T>> getForEntity(Entity entity) {
        if (entity instanceof RaceContainerHolder) {
            return Optional.of(((RaceContainerHolder<T>)entity));
        }
        return Optional.empty();
    }
}
