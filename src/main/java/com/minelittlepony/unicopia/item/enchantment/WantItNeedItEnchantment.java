package com.minelittlepony.unicopia.item.enchantment;

import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.particle.FollowingParticleEffect;
import com.minelittlepony.unicopia.particle.ParticleUtils;
import com.minelittlepony.unicopia.particle.UParticles;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class WantItNeedItEnchantment extends SimpleEnchantment {

    protected WantItNeedItEnchantment(Options options) {
        super(options);
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

    public static int getLevel(Entity entity) {
        return entity instanceof LivingEntity l ? getLevel(l)
             : entity instanceof ItemEntity i ? getLevel(i)
             : 0;
    }

    public static int getLevel(ItemEntity entity) {
        return EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, entity.getStack());
    }

    public static int getLevel(LivingEntity entity) {
        return EnchantmentHelper.getEquipmentLevel(UEnchantments.WANT_IT_NEED_IT, entity)
                + EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, entity.getOffHandStack())
                + EnchantmentHelper.getLevel(UEnchantments.WANT_IT_NEED_IT, entity.getMainHandStack());
    }
}
