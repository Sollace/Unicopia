package com.minelittlepony.unicopia.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.enchantment.SimpleEnchantment;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;

public class Enchantments implements NbtSerialisable, Tickable {

    private final Living<?> entity;

    private final Set<RegistryEntry<Enchantment>> equippedEnchantments = new HashSet<>();

    private final Map<RegistryEntry<Enchantment>, SimpleEnchantment.Data> data = new HashMap<>();

    Enchantments(Living<?> entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T extends SimpleEnchantment.Data> Optional<T> getOrEmpty(RegistryEntry<Enchantment> enchantment) {
        return Optional.ofNullable((T)data.get(enchantment));
    }

    @SuppressWarnings("unchecked")
    public <T extends SimpleEnchantment.Data> T computeIfAbsent(RegistryEntry<Enchantment> enchantment, Supplier<T> factory) {
        return (T)data.computeIfAbsent(enchantment, e -> factory.get());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends SimpleEnchantment.Data> T remove(RegistryEntry<Enchantment> enchantment) {
        return (T)data.remove(enchantment);
    }

    @Override
    public void tick() {
        UEnchantments.REGISTRY.forEach(ench -> {
            int level = EnchantmentHelper.getEquipmentLevel(ench, entity.asEntity());

            boolean active = level > 0;

            if (active != equippedEnchantments.contains(ench)) {
                if (active) {
                    equippedEnchantments.add(ench);
                    ench.value().onEquipped(entity);
                } else {
                    equippedEnchantments.remove(ench);
                    ench.value().onUnequipped(entity);
                }
            }

            if (active) {
                ench.value().onUserTick(entity, level);
            }
        });
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        NbtList list = new NbtList();
        equippedEnchantments.forEach(enchant -> {
            enchant.getKey().ifPresent(key -> {
                list.add(NbtString.of(key.getValue().toString()));
            });
        });
        compound.put("enchants", list);
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        equippedEnchantments.clear();
        if (compound.contains("enchants")) {
            compound.getList("enchants", NbtElement.STRING_TYPE).forEach(tag -> {
                lookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                    .getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(tag.asString())))
                    .ifPresent(equippedEnchantments::add);
            });
        }
    }
}
