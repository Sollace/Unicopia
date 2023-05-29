package com.minelittlepony.unicopia.ability.magic.spell.effect;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;

import net.minecraft.entity.Entity;

public class ChillingBreathSpell extends AbstractSpell implements HomingSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.ICE, 15)
            .build();

    protected ChillingBreathSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.subtractEnergyCost(90)) {
            setDead();
        }
        return !isDead();
    }

    @Override
    public boolean setTarget(Entity target) {
        if (target instanceof LavaAffine affine) {
            affine.setLavaAffine(!affine.isLavaAffine());
        }
        return true;
    }
}
