package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public record AltarRecipeMatch(
        ItemEntity target,
        List<ItemEntity> ingredients,
        ItemStack result
    ) {

    @Nullable
    public static AltarRecipeMatch of(List<ItemEntity> inputs) {
        ItemEntity clock = inputs.stream().filter(item -> item.getStack().isOf(Items.CLOCK)).findFirst().orElse(null);

        if (clock != null) {
            return new AltarRecipeMatch(clock, List.of(), UItems.SPECTRAL_CLOCK.getDefaultStack());
        }

        return null;
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
