package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.particle.ParticleTypes;

public class KirinCastingAbility extends UnicornCastingAbility {
    @Override
    public void coolDown(Pony player, AbilitySlot slot) {
        player.spawnParticles(ParticleTypes.FLAME, 5);
    }

    @Override
    protected boolean canCast(SpellType<?> type) {
        return type == SpellType.FIRE_BOLT || type == SpellType.FLAME || type == SpellType.INFERNAL;
    }
}
