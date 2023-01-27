package com.minelittlepony.unicopia;

import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;

public interface UGameRules {
    GameRules.Key<BooleanRule> SWAP_TRIBE_ON_DEATH = GameRules.register("swapTribeOnDeath", GameRules.Category.SPAWNING, BooleanRule.create(false));
    GameRules.Key<BooleanRule> ANNOUNCE_TRIBE_JOINS = GameRules.register("announceTribeJoins", GameRules.Category.SPAWNING, BooleanRule.create(false));

    static void bootstrap() { }
}
