package com.minelittlepony.unicopia;

import java.util.Set;

public class AllowList {
    public static final AllowList INSTANCE = new AllowList();

    public AllowList() {

    }

    public boolean disable() {
        if (!isEnabled()) {
            return false;
        }
        Unicopia.getConfig().speciesWhiteList.get().clear();
        Unicopia.getConfig().save();
        return true;
    }

    public boolean isEnabled() {
        return !Unicopia.getConfig().speciesWhiteList.get().isEmpty();
    }

    public boolean add(Race race) {
        if (race.isUnset() || race.isHuman()) {
            return false;
        }
        Set<String> values = Unicopia.getConfig().speciesWhiteList.get();
        boolean added = values.add(race.getId().toString());
        Unicopia.getConfig().save();
        return added;
    }

    public boolean remove(Race race) {
        Set<String> values = Unicopia.getConfig().speciesWhiteList.get();
        if (values.isEmpty()) {
            for (Race r : Race.REGISTRY) {
                if (!r.isUnset() && r != race) {
                    values.add(r.getId().toString());
                }
            }
            Unicopia.getConfig().save();
            return true;
        }
        boolean removed = values.remove(race.getId().toString());
        Unicopia.getConfig().save();
        return removed;
    }

    public boolean permits(Race race) {
        return race.isUnset()
                || race.isHuman()
                || !isEnabled()
                || Unicopia.getConfig().speciesWhiteList.get().contains(race.getId().toString());
    }
}
