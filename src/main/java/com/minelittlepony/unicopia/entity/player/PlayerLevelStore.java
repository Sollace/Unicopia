package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.ability.magic.Levelled;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import net.minecraft.sound.*;
import net.minecraft.util.math.MathHelper;

class PlayerLevelStore implements Levelled.LevelStore {

    private final Pony pony;

    private final DataTracker.Entry<Integer> dataEntry;

    private final boolean upgradeMana;

    private final SoundEvent levelUpSound;

    PlayerLevelStore(Pony pony, DataTracker tracker, boolean upgradeMana, SoundEvent levelUpSound) {
        this.pony = pony;
        this.dataEntry = tracker.startTracking(TrackableDataType.INT, 0);
        this.upgradeMana = upgradeMana;
        this.levelUpSound = levelUpSound;
    }

    @Override
    public void add(int levels) {
        Levelled.LevelStore.super.add(levels);
        if (levels > 0) {
            if (upgradeMana) {
                pony.getMagicalReserves().getMana().addPercent(50);
            }
            pony.asWorld().playSound(null, pony.getOrigin(), levelUpSound, SoundCategory.PLAYERS, 1, 2);
        }
    }

    @Override
    public int getMax() {
        return 999; // -1 because displayed levels start at 1
    }

    @Override
    public int get() {
        return dataEntry.get();
    }

    @Override
    public void set(int level) {
        dataEntry.set(MathHelper.clamp(level, 0, getMax()));
    }
}
