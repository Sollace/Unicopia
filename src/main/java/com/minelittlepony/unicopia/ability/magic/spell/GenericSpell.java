package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.particle.MagicParticleEffect;

public class GenericSpell extends AbstractSpell {

    protected GenericSpell(SpellType<?> type, Affinity affinity) {
        super(type, affinity);
    }

    @Override
    public boolean update(Caster<?> source) {
        return true;
    }

    @Override
    public void render(Caster<?> source) {
        source.spawnParticles(new MagicParticleEffect(getType().getColor()), 1);
    }
}
