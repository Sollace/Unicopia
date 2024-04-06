package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.inventory.SpellbookInventory;
import com.minelittlepony.unicopia.item.*;
import com.minelittlepony.unicopia.recipe.URecipes;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Recipe for adding traits to an existing spell.
 */
public class SpellEnhancingRecipe implements SpellbookRecipe {
    private final Identifier id;

    private final IngredientWithSpell material;

    private SpellEnhancingRecipe(Identifier id, IngredientWithSpell material) {
        this.id = id;
        this.material = material;
    }

    public IngredientWithSpell getBaseMaterial() {
        return material;
    }

    @Override
    public void buildCraftingTree(CraftingTreeBuilder builder) {
        builder.input(material.getMatchingStacks());
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean matches(SpellbookInventory inventory, World world) {
        ItemStack stack = inventory.getItemToModify();
        return material.test(stack) && EnchantableItem.isEnchanted(stack);
    }

    @Override
    public ItemStack craft(SpellbookInventory inventory, DynamicRegistryManager registries) {
        return SpellTraits.of(inventory.getItemToModify())
                .add(inventory.getTraits())
                .applyTo(inventory.getItemToModify());
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registries) {
        return UItems.GEMSTONE.getDefaultStack();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.TRAIT_COMBINING;
    }

    public static class Serializer implements RecipeSerializer<SpellEnhancingRecipe> {
        @Override
        public SpellEnhancingRecipe read(Identifier id, JsonObject json) {
            return new SpellEnhancingRecipe(id, IngredientWithSpell.fromJson(json.get("material")));
        }

        @Override
        public SpellEnhancingRecipe read(Identifier id, PacketByteBuf buf) {
            return new SpellEnhancingRecipe(id, IngredientWithSpell.fromPacket(buf));
        }

        @Override
        public void write(PacketByteBuf buf, SpellEnhancingRecipe recipe) {
            recipe.material.write(buf);
        }
    }
}
