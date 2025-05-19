package com.minelittlepony.unicopia.entity.behaviour;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.mob.SpellcastingIllagerEntity;

public class SpellcastingIllagerBehaviour extends EntityBehaviour<SpellcastingIllagerEntity> {
    @Override
    public void update(Pony player, SpellcastingIllagerEntity entity, Disguise s) {
        if (player.sneakingChanged()) {
            SpellCastAccess.setSpell(player, entity, s);
        }
    }

    private static abstract class SpellCastAccess extends SpellcastingIllagerEntity {
        SpellCastAccess() {super(null, null);}

        static void setSpell(Pony player, SpellcastingIllagerEntity entity, Disguise s) {
            if (player.asEntity().isSneaking()) {
                SpellcastingIllagerEntity.Spell[] spells = SpellcastingIllagerEntity.Spell.values();
                SpellcastingIllagerEntity.Spell spell = spells[entity.getWorld().random.nextInt(spells.length - 1) + 1];

                entity.setSpell(spell);
                entity.setTarget(entity);
            } else {
                entity.setSpell(SpellcastingIllagerEntity.Spell.NONE);
                entity.setTarget(null);
            }
        }
    }
}
