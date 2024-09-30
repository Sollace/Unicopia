package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.mob.HostileEntity;

public class StressfulEnchantment {

    public void onUserTick(Living<?> user, int level) {
        if (user instanceof Pony pony && pony.asEntity().age % 10 == 0) {
            int range = (level + 1) * 3;
            if (pony.asWorld().getEntitiesByClass(HostileEntity.class, user.asEntity().getBoundingBox().expand(range, 0, range), enemy -> {
                return enemy != null
                        && enemy.canTarget(user.asEntity())
                        && enemy.canSee(user.asEntity())
                        && enemy.getTarget() == user.asEntity();
            }).isEmpty()) {
                return;
            }

            Bar bar = pony.getMagicalReserves().getEnergy();
            float targetPercent = (level / (float)user.entryFor(UEnchantments.STRESSED).value().definition().maxLevel()) * 0.05125F;
            float increase = 1F + (level * level)/100F;
            if (bar.getPercentFill() < targetPercent) {
                bar.add(increase);
            }
        }
    }
}
