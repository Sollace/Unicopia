package com.minelittlepony.unicopia;

import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;

public interface UGameRules {
    GameRules.Key<BooleanRule> SWAP_TRIBE_ON_DEATH = GameRules.register("swapTribeOnDeath", GameRules.Category.SPAWNING, BooleanRule.create(false));

    static void bootstrap() { }
}
