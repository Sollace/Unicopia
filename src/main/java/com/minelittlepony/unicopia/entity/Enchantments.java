package com.minelittlepony.unicopia.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.world.ServerWorld;

// TODO: Use a EnchantmentLocationBasedEffect for this instead
@Deprecated
public class Enchantments implements NbtSerialisable, Tickable {

    private final Living<?> entity;

    private final Set<RegistryKey<Enchantment>> equippedEnchantments = new HashSet<>();

    private final Map<RegistryKey<Enchantment>, Data> data = new HashMap<>();

    Enchantments(Living<?> entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T extends Data> Optional<T> getOrEmpty(RegistryKey<Enchantment> enchantment) {
        return Optional.ofNullable((T)data.get(enchantment));
    }

    @SuppressWarnings("unchecked")
    public <T extends Data> T computeIfAbsent(RegistryKey<Enchantment> enchantment, Supplier<T> factory) {
        return (T)data.computeIfAbsent(enchantment, e -> factory.get());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Data> T remove(RegistryKey<Enchantment> enchantment) {
        return (T)data.remove(enchantment);
    }

    @Override
    public void tick() {
        UEnchantments.REGISTRY.forEach(key -> {
            var ench = entity.entryFor(key);
            int level = EnchantmentHelper.getEquipmentLevel(ench, entity.asEntity());

            boolean active = level > 0;

            if (active != equippedEnchantments.contains(key)) {
                if (active) {
                    equippedEnchantments.add(key);
                    ench.value().applyLocationBasedEffects((ServerWorld)entity.asWorld(), level, null, entity.asEntity());
                } else {
                    equippedEnchantments.remove(key);
                    ench.value().removeLocationBasedEffects(level, null, entity.asEntity());
                }
            }

            if (active) {
                ench.value().onTick((ServerWorld)entity.asWorld(), level, null, entity.asEntity());
            }
        });
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        NbtList list = new NbtList();
        equippedEnchantments.forEach(key -> {
            list.add(NbtString.of(key.getValue().toString()));
        });
        compound.put("enchants", list);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        equippedEnchantments.clear();
        if (compound.contains("enchants")) {
            compound.getList("enchants", NbtElement.STRING_TYPE).forEach(tag -> {
                equippedEnchantments.add(RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(tag.asString())));
            });
        }
    }

    public static class Data {
        public float level;
    }
}
