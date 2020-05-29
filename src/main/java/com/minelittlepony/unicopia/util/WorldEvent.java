package com.minelittlepony.unicopia.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * All of the Auxiliary effects used in minecraft for World.playGlobalEvent
 */
public enum WorldEvent {
    DISPENSER_DISPENSE_BLOCK(1000),
    DISPENSER_FAIL(1001),
    DISPENSE_SHOOT_PROJECTILE(1002),
    LAUNCH_ENDER_PEAR(1003),
    LAUNCH_FIREWORKS_ROCKET(1004),
    RECORD_DROP(1005), IRON_DOOR_OPEN(1005),
    WOODEN_DOOR_OPEN(1006),
    WOODEN_TRAPDOOR_OPEN(1007),
    GATE_OPEN(1008),
    FIRE_EXTENGUISH(1009),
    PLAY_RECORD(1010),
    IRON_DOOR_SLAM(1011),
    WOODEN_DOOR_SLAM(1012),
    WOODEN_TRAPDOOR_SLAM(1013),
    FENCE_GATE_SWIVEL(1014),
    GHAST_SCREAM(1015),
    GHAST_SHOOT(1016),
    ENDERMAN_SCREAM(1017),
    FIRE_SHOOT(1018),
    DOOR_SWIVEL(1019), WOOD_DOOR_KNOCK(1019),
    REPAIR_ITEM(1020), IRON_DOOR_KNOCK(1020),
    DOOR_BROKEN(1021),
    WITHER_ATTACK(1022),
    WITHER_SHOOT(1024),
    ENTITY_TAKEOFF(1025),
    MOB_INFECT(1026),
    MOB_CURE(1027),
    ANVIL_DESTROY(1029),
    ANVIL_USE(1030),
    ANVIL_LAND(1031),
    PORTAL_WARP(1032),
    ORGANIC_WET(1033),
    ORGANIC_DRY(1034),
    BREW_POTION(1035),
    DOOR_CLOSE(1036),
    DOOR_OPEN(1037),






    DISPENSE_PARTICLES(2000),
    DESTROY_BLOCK(2001),
    XP_POP(2002), PROJECTILE_HIT(2002),
    EYE_OF_ENDER(2003),
    MOB_SPAWN(2004),
    BONEMEAL(2005),
    DRAGON_BREATH(2006),
    POTION_INSTANT(2007),
    DRAGON_DEFEATED(3000),
    DRAGON_ROARS(3001),

    UNKNOWN(0);

    private final int id;

    WorldEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void play(World world, BlockPos pos, BlockState state) {
        play(world, pos, Block.getRawIdFromState(state));
    }

    public void play(World world, BlockPos pos) {
        play(world, pos, 0);
    }

    private void play(World world, BlockPos pos, int data) {
        world.playLevelEvent(getId(), pos, data);
    }
}
