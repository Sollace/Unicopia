package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.mob.AirBalloonEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public interface Debug {
    boolean SPELLBOOK_CHAPTERS = Boolean.getBoolean("unicopia.debug.spellbookChapters");
    boolean CHECK_GAME_VALUES = Boolean.getBoolean("unicopia.debug.checkGameValues");

    boolean[] TESTS_COMPLETE = {false};

    static void runTests(World world) {
        if (!CHECK_GAME_VALUES || TESTS_COMPLETE[0]) {
            return;
        }
        TESTS_COMPLETE[0] = true;

        try {
            for (var type : BoatEntity.Type.values()) {
                var balloon = UEntities.AIR_BALLOON.create(world);
                balloon.setBasketType(AirBalloonEntity.BasketType.of(type));
                balloon.asItem();
            }
        } catch (Throwable t) {
            throw new IllegalStateException("Tests failed", t);
        }
    }
}
