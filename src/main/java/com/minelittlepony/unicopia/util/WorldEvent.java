package com.minelittlepony.unicopia.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

/**
 * All of the Auxiliary effects used in minecraft for World.playGlobalEvent
 */
public interface WorldEvent {
    int
        DISPENSER_SUCCESS = 1000,
        DISPENSER_FAIL = 1001,
        DISPENSER_LAUNCH = 1002,
        ENDER_EYE_LAUNCH = 1003,
        FIREWORKS_ROCKET_SHOOT = 1004,
        IRON_DOOR_OPEN = 1005,
        WOODEN_DOOR_OPEN = 1006,
        WOODEN_TRAPDOOR_OPEN = 1007,
        FENCE_GATE_OPEN = 1008,
        FIRE_EXTINGUISH = 1009,
        MUSIC_DISC_PLAY = 1010,
        IRON_DOOR_CLOSE = 1011,
        WOODEN_DOOR_CLOSE = 1012,
        WOODEN_TRAPDOOR_CLOSE = 1013,
        FENCE_GATE_CLOSE = 1014,
        GHAST_WARN = 1015,
        GHAST_SHOOT = 1016,
        ENDER_DRAGON_SHOOT = 1017,
        BLAZE_SHOOT = 1018,
        ZOMBIE_ATTACK_WOODEN_DOOR = 1019,
        ZOMBIE_ATTACK_IRON_DOOR = 1020,
        ZOMBIE_BREAK_WOODEN_DOOR = 1021,
        WITHER_BREAK_BLOCK = 1022,
        WITHER_SHOOT = 1024,
        BAT_TAKEOFF = 1025,
        ZOMBIE_INFECT = 1026,
        ZOMBIE_VILLAGER_CONVERTED = 1027,
        ANVIL_DESTROY = 1029,
        ANVIL_USE = 1030,
        ANVIL_LAND = 1031,
        PORTAL_TRAVEL = 1032,
        CHORUS_FLOWER_GROW = 1033,
        CHORUS_FLOWER_DEATH = 1034,
        BREWING_STAND_BREW = 1035,
        IRON_TRAPDOOR_CLOSE = 1036,
        IRON_TRAPDOOR_OPEN = 1037,

        PHANTOM_BITE = 1039,
        ZOMBIE_CONVERTED_TO_DROWNED = 1040,
        HUSK_CONVERTED_TO_ZOMBIE = 1041,
        GRINDSTONE_USE = 1042,
        BOOK_PAGE_TURN = 2043,
        SMITHING_TABLE_USE = 1044,
        COMPOSTER_COMPOST = 1500,
        LAVA_EXTINGUISH = 1501,
        REDSTONE_TORCH_BURNOUT = 1502,
        END_PORTAL_FRAME_FILL = 1503,

        DISPENSER_PARTICLES = 2000,
        DESTROY_BLOCK = 2001,
        XP_POP = 2002, PROJECTILE_HIT = 2002,
        EYE_OF_ENDER = 2003,
        MOB_SPAWNED = 2004,
        BONEMEAL = 2005,
        DRAGON_BREATH = 2006,
        POTION_INSTANT = 2007,
        ENDER_DRAGON_ATTACK = 2008,
        SPONGE_SUCK = 2009,
        ENDER_DRAGON_DEFEATED = 3000,
        ENDER_DRAGON_GROWL = 3001,

        UNKNOWN = 0;

    static void play(int event, WorldAccess world, BlockPos pos, BlockState state) {
        world.syncWorldEvent(event, pos, Block.getRawIdFromState(state));
    }

    static void play(int event, WorldAccess world, BlockPos pos) {
        world.syncWorldEvent(event, pos, 0);
    }

}
