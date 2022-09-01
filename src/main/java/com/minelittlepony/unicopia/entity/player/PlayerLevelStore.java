package com.minelittlepony.unicopia.entity.player;

import com.minelittlepony.unicopia.ability.magic.Levelled;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

class PlayerLevelStore implements Levelled.LevelStore {

    private static final TrackedData<Integer> LEVEL = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private final Pony pony;

    PlayerLevelStore(Pony pony) {
        this.pony = pony;
        pony.getEntity().getDataTracker().startTracking(LEVEL, 0);
    }

    @Override
    public void add(int levels) {
        if (levels > 0) {
            pony.getMagicalReserves().getMana().add(pony.getMagicalReserves().getMana().getMax() / 2);
            pony.getReferenceWorld().playSound(null, pony.getOrigin(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1, 2);
        }
        Levelled.LevelStore.super.add(levels);
    }

    @Override
    public int getMax() {
        return 900;
    }

    @Override
    public int get() {
        return pony.getEntity().getDataTracker().get(LEVEL);
    }

    @Override
    public void set(int level) {
        pony.getEntity().getDataTracker().set(LEVEL, MathHelper.clamp(level, 0, getMax()));
    }
}
