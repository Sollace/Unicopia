package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.EquipmentSlot;

public class StressfulEnchantment extends SimpleEnchantment {

    protected StressfulEnchantment() {
        super(Rarity.COMMON, true, 3, EquipmentSlot.values());
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (user instanceof Pony) {
            Bar bar = ((Pony)user).getMagicalReserves().getEnergy();
            float targetPercent = (level / (float)getMaxLevel()) * 0.5F;
            if (bar.getPercentFill() < targetPercent) {
                bar.add(20);
            }
        }
    }
}
