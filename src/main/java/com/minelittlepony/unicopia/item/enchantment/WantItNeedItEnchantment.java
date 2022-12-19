package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.entity.EquipmentSlot;

public class WantItNeedItEnchantment extends SimpleEnchantment {

    protected WantItNeedItEnchantment() {
        super(Rarity.VERY_RARE, true, 1, EquipmentSlot.values());
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (user instanceof Creature && user.getReferenceWorld().random.nextInt(10) == 0) {
            ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, user.asEntity(), 0.2F), user.asEntity(), 1);
        }
    }
}
