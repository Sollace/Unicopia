package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.sound.SoundEvents;

public class TraderBehaviour extends EntityBehaviour<AbstractTraderEntity> {
    @Override
    public void update(Pony pony, AbstractTraderEntity entity, DisguiseSpell spell) {
        if (pony.sneakingChanged() && pony.getMaster().isSneaking()) {
            entity.setHeadRollingTimeLeft(40);

            if (!entity.world.isClient()) {
               entity.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1, 1);
            }
        }
    }
}
