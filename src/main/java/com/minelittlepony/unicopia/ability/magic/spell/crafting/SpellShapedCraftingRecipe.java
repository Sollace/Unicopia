package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public class SpellShapedCraftingRecipe extends ShapedRecipe {

    public SpellShapedCraftingRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput(null));
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        return InventoryUtil.stream(inventory)
            .filter(stack -> stack.getItem() instanceof EnchantableItem)
            .filter(EnchantableItem::isEnchanted)
            .map(stack -> ((EnchantableItem)stack.getItem()).getSpellEffect(stack))
            .findFirst()
            .map(spell -> spell.traits().applyTo(EnchantableItem.enchant(super.craft(inventory, registries), spell.type())))
            .orElseGet(() -> super.craft(inventory, registries));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.CRAFTING_MAGICAL_SERIALIZER;
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public ShapedRecipe read(Identifier id, JsonObject json) {
            return new SpellShapedCraftingRecipe(super.read(id, json));
        }

        @Override
        public ShapedRecipe read(Identifier id, PacketByteBuf buffer) {
            return new SpellShapedCraftingRecipe(super.read(id, buffer));
        }
    }
}
