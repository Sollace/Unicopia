package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.UItems;
import com.mojang.datafixers.util.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public record AltarRecipeMatch(
        ItemEntity target,
        List<ItemEntity> ingredients,
        ItemStack result
    ) {

    public static final Map<Item, Item> RECIPES = Map.of(
            Items.CLOCK, UItems.SPECTRAL_CLOCK,
            Items.TOTEM_OF_UNDYING, UItems.TOTEM_OF_DYING
    );

    @Nullable
    public static AltarRecipeMatch of(List<ItemEntity> inputs) {
        return inputs.stream()
                .map(item -> Pair.of(item, RECIPES.get(item.getStack().getItem())))
                .filter(pair -> pair.getSecond() != null)
                .map(pair ->  new AltarRecipeMatch(pair.getFirst(), List.of(), pair.getSecond().getDefaultStack()))
                .findFirst()
                .orElse(null);
    }

    public boolean isRemoved() {
        return target.isRemoved() || ingredients.stream().anyMatch(ItemEntity::isRemoved);
    }

    public void craft() {
        ItemStack clockStack = result.copyWithCount(target.getStack().getCount());
        target.setStack(clockStack);
        target.setInvulnerable(true);
        ingredients.forEach(Entity::discard);
    }
}
