package com.minelittlepony.unicopia.server.world;

import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.IntRule;

public interface UGameRules {
    GameRules.Key<BooleanRule> SWAP_TRIBE_ON_DEATH = GameRules.register("swapTribeOnDeath", GameRules.Category.SPAWNING, BooleanRule.create(false));
    GameRules.Key<BooleanRule> ANNOUNCE_TRIBE_JOINS = GameRules.register("announceTribeJoins", GameRules.Category.SPAWNING, BooleanRule.create(false));
    GameRules.Key<BooleanRule> DO_NOCTURNAL_BAT_PONIES = GameRules.register("doNocturnalBatPonies", GameRules.Category.PLAYER, BooleanRule.create(true));
    GameRules.Key<IntRule> WEATHER_EFFECTS_STRENGTH = GameRules.register("weatherEffectsStrength", GameRules.Category.MISC, IntRule.create(100));

    static void bootstrap() { }
}
