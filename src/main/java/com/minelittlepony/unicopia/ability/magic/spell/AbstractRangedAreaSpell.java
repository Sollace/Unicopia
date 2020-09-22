package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.AttachableSpell;
import com.minelittlepony.unicopia.ability.magic.Caster;

public abstract class AbstractRangedAreaSpell extends AbstractSpell implements AttachableSpell {

    @Override
    public int getMaxLevelCutOff(Caster<?> source) {
        return 17;
    }

    @Override
    public float getMaxExhaustion(Caster<?> caster) {
        return 1000;
    }

    @Override
    public float getExhaustion(Caster<?> caster) {
        float max = getMaxLevelCutOff(caster);
        float current = caster.getCurrentLevel();

        if (current > max) {
            float maxEc = getMaxExhaustion(caster);

            current -= max;
            current /= max;
            current /= maxEc;

            return maxEc - current;
        }

        return super.getExhaustion(caster);
    }

    @Override
    public boolean updateOnPerson(Caster<?> caster) {
        return update(caster);
    }

    @Override
    public void renderOnPerson(Caster<?> caster) {
        render(caster);
    }
}