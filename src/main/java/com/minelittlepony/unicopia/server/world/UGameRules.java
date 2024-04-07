package com.minelittlepony.unicopia.server.world;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.IntRule;

public interface UGameRules {
    GameRules.Key<BooleanRule> SWAP_TRIBE_ON_DEATH = GameRuleRegistry.register("swapTribeOnDeath", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(false));
    GameRules.Key<BooleanRule> ANNOUNCE_TRIBE_JOINS = GameRuleRegistry.register("announceTribeJoins", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(false));
    GameRules.Key<BooleanRule> DO_NOCTURNAL_BAT_PONIES = GameRuleRegistry.register("doNocturnalBatPonies", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
    GameRules.Key<IntRule> WEATHER_EFFECTS_STRENGTH = GameRuleRegistry.register("weatherEffectsStrength", GameRules.Category.MISC, GameRuleFactory.createIntRule(100, 0, 100));
    GameRules.Key<BooleanRule> DO_TIME_MAGIC = GameRuleRegistry.register("doTimeMagic", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));
    GameRules.Key<IntRule> MAX_DARK_VORTEX_SIZE = GameRuleRegistry.register("maxDarkVortexSize", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(20, 1, 25));

    static void bootstrap() { }
}
