package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

public class WantItNeedItEnchantment extends SimpleEnchantment {

    protected WantItNeedItEnchantment(Options options) {
        super(options, UEnchantmentValidSlots.ANY);
    }

    @Override
    public void onUserTick(Living<?> user, int level) {
        if (user instanceof Creature && user.asWorld().random.nextInt(10) == 0) {
            ParticleUtils.spawnParticles(new FollowingParticleEffect(UParticles.HEALTH_DRAIN, user.asEntity(), 0.2F), user.asEntity(), 1);
        }
    }

    public static boolean prefersEquipment(ItemStack newStack, ItemStack oldStack) {
        int newLevel = EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, newStack);
        int oldLevel = EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, oldStack);
        return newLevel > oldLevel;
    }
}
