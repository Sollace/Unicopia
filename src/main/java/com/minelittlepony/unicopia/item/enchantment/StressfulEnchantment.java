package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;

import com.minelittlepony.unicopia.entity.player.Pony;

public class StressfulEnchantment extends SimpleEnchantment {

    protected StressfulEnchantment(Options options) {
        super(options);
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (user instanceof Pony) {
            Bar bar = ((Pony)user).getMagicalReserves().getEnergy();
            float targetPercent = (level / (float)getMaxLevel()) * 0.05125F;
            float increase = 1F + (level * level)/100F;
            if (bar.getPercentFill() < targetPercent) {
                bar.add(increase);
            }
        }
    }
}
