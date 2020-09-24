package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.sound.SoundEvents;

public class VillagerBehaviour extends EntityBehaviour<AbstractTraderEntity> {
    @Override
    public void update(Caster<?> source, AbstractTraderEntity entity, Spell spell) {

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            if (player.sneakingChanged() && player.getOwner().isSneaking()) {
                entity.setHeadRollingTimeLeft(40);

                if (!entity.world.isClient()) {
                   entity.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1, 1);
                }
            }
        }
    }
}
