package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import com.minelittlepony.unicopia.recipe.URecipes;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntryList;

@Mixin(Ingredient.class)
abstract class MixinIngredient {
    @ModifyVariable(method = "<init>", at = @At("HEAD"))
    private static RegistryEntryList<Item> onInit(RegistryEntryList<Item> entries) {
        return entries.getStorage().right().filter(stacks -> stacks.size() == 1).flatMap(stacks -> {
            return URecipes.getRecipeContentsReplacement(stacks.getFirst());
        }).orElse(entries);
    }
}