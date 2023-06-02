package com.minelittlepony.unicopia.entity.damage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.WorldConvertable;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.util.MagicalDamageSource;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public interface UDamageSources extends WorldConvertable {

    static UDamageSources of(World world) {
        return () -> world;
    }

    private static RegistryEntry<DamageType> entryOf(WorldConvertable world, RegistryKey<DamageType> type) {
        return world.asWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(type);
    }

    default MagicalDamageSource damageOf(RegistryKey<DamageType> type) {
        return new MagicalDamageSource(entryOf(this, type), null);
    }

    default MagicalDamageSource damageOf(RegistryKey<DamageType> type, @Nullable LivingEntity source) {
        return new MagicalDamageSource(entryOf(this, type), source, null);
    }

    default MagicalDamageSource damageOf(RegistryKey<DamageType> type, @NotNull Caster<?> caster) {
        return new MagicalDamageSource(entryOf(this, type), caster.asEntity(), caster.getMaster(), caster);
    }
}
