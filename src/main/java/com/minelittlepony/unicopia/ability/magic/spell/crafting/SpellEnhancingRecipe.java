package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.item.*;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.minelittlepony.unicopia.recipe.URecipes;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

/**
 * Recipe for adding traits to an existing spell.
 */
public record SpellEnhancingRecipe (IngredientWithSpell material) implements SpellbookRecipe {
    public static final MapCodec<SpellEnhancingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IngredientWithSpell.CODEC.fieldOf("material").forGetter(recipe -> recipe.material)
    ).apply(instance, SpellEnhancingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, SpellEnhancingRecipe> PACKET_CODEC = IngredientWithSpell.PACKET_CODEC.xmap(SpellEnhancingRecipe::new, SpellEnhancingRecipe::material);

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
    public boolean matches(Input inventory, World world) {
        ItemStack stack = inventory.stackToModify();
        return material.test(stack) && EnchantableItem.isEnchanted(stack);
    }

    @Override
    public ItemStack craft(Input inventory, WrapperLookup registries) {
        return SpellTraits.of(inventory.stackToModify())
                .add(inventory.traits())
                .applyTo(inventory.stackToModify());
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getResult(WrapperLookup registries) {
        return UItems.GEMSTONE.getDefaultStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.TRAIT_COMBINING;
    }
}
