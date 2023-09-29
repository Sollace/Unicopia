package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.function.Function;

import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.serialization.Codec;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;

public class SpellShapedCraftingRecipe extends ShapedRecipe {

    public SpellShapedCraftingRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.getCategory(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getResult(null));
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
        public Codec<ShapedRecipe> codec() {
            return super.codec().xmap(SpellShapedCraftingRecipe::new, Function.identity());
        }

        @Override
        public ShapedRecipe read(PacketByteBuf buffer) {
            return new SpellShapedCraftingRecipe(super.read(buffer));
        }
    }
}
