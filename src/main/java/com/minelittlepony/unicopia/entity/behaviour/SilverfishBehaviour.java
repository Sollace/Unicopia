package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.block.state.StateMaps;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.WorldEvent;

import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.math.BlockPos;

public class SilverfishBehaviour extends EntityBehaviour<SilverfishEntity> {
    @Override
    public void update(Caster<?> source, SilverfishEntity entity, Spell spell) {
        if (source instanceof Pony && !source.isClient()) {
            Pony player = (Pony)source;

            if (player.sneakingChanged() && player.getOwner().isSneaking()) {
                BlockPos pos = entity.getBlockPos().down();
                BlockState state = entity.world.getBlockState(pos);

                if (StateMaps.SILVERFISH_AFFECTED.canConvert(state)) {

                    entity.world.setBlockState(pos, StateMaps.SILVERFISH_AFFECTED.getConverted(state));
                    WorldEvent.play(WorldEvent.DESTROY_BLOCK, entity.world, pos, state);
                }
            }
        }
    }
}
