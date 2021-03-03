package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.WorldEvent;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.math.BlockPos;

public class SilverfishBehaviour extends EntityBehaviour<SilverfishEntity> {
    @Override
    public void update(Pony player, SilverfishEntity entity, DisguiseSpell spell) {
        if (!player.isClient() && player.sneakingChanged() && player.getMaster().isSneaking()) {
            BlockPos pos = entity.getBlockPos().down();
            BlockState state = entity.world.getBlockState(pos);

            if (StateMaps.SILVERFISH_AFFECTED.convert(entity.world, pos)) {
                WorldEvent.play(WorldEvent.DESTROY_BLOCK, entity.world, pos, state);
            }
        }
    }
}
