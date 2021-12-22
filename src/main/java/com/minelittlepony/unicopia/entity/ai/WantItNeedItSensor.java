package com.minelittlepony.unicopia.entity.ai;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.server.world.ServerWorld;

public class WantItNeedItSensor extends Sensor<LivingEntity> {

    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleType.VISIBLE_MOBS);
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        entity.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).ifPresent(targets -> {
            entity.getBrain().remember(MemoryModuleType.ATTACK_TARGET, targets
                    .findFirst(e -> (EnchantmentHelper.getEquipmentLevel(UEnchantments.WANT_IT_NEED_IT, e) * 10) >= entity.distanceTo(e)));
        });
    }

}
