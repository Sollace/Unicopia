package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.mob.SpellcastingIllagerEntity;

public class SpellcastingIllagerBehaviour extends EntityBehaviour<SpellcastingIllagerEntity> {
    @Override
    public void update(Caster<?> source, SpellcastingIllagerEntity entity) {

        if (source instanceof Pony) {
            Pony player = (Pony)source;

            if (player.sneakingChanged()) {
                if (player.getOwner().isSneaking()) {
                    SpellcastingIllagerEntity.Spell[] spells = SpellcastingIllagerEntity.Spell.values();
                    SpellcastingIllagerEntity.Spell spell = spells[entity.world.random.nextInt(spells.length - 1) + 1];

                    entity.setSpell(spell);
                    entity.setTarget(entity);
                } else {
                    entity.setSpell(SpellcastingIllagerEntity.Spell.NONE);
                    entity.setTarget(null);
                }
            }
        }
    }
}
