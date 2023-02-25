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
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Enchantments implements NbtSerialisable, Tickable {

    private final Living<?> entity;

    private final Set<Enchantment> equippedEnchantments = new HashSet<>();

    private final Map<Enchantment, SimpleEnchantment.Data> data = new HashMap<>();

    Enchantments(Living<?> entity) {
        this.entity = entity;
    }

    @SuppressWarnings("unchecked")
    public <T extends SimpleEnchantment.Data> Optional<T> getOrEmpty(Enchantment enchantment) {
        return Optional.ofNullable((T)data.get(enchantment));
    }

    @SuppressWarnings("unchecked")
    public <T extends SimpleEnchantment.Data> T computeIfAbsent(Enchantment enchantment, Supplier<T> factory) {
        return (T)data.computeIfAbsent(enchantment, e -> factory.get());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends SimpleEnchantment.Data> T remove(Enchantment enchantment) {
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
                    ench.onEquipped(entity);
                } else {
                    equippedEnchantments.remove(ench);
                    ench.onUnequipped(entity);
                }
            }

            if (active) {
                ench.onUserTick(entity, level);
            }
        });
    }

    @Override
    public void toNBT(NbtCompound compound) {
        NbtList list = new NbtList();
        equippedEnchantments.forEach(enchant -> {
            Identifier id = Registry.ENCHANTMENT.getId(enchant);
            if (id != null) {
                list.add(NbtString.of(id.toString()));
            }
        });
        compound.put("enchants", list);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        equippedEnchantments.clear();
        if (compound.contains("enchants")) {
            compound.getList("enchants", 8).forEach(tag -> {
                Registry.ENCHANTMENT.getOrEmpty(new Identifier(tag.asString())).ifPresent(equippedEnchantments::add);
            });
        }
    }
}
