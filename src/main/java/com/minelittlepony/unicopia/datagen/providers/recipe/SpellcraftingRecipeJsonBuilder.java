package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellCraftingRecipe;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.TraitIngredient;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.advancement.TraitDiscoveredCriterion;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.UItems;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class SpellcraftingRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;

    private IngredientWithSpell base = IngredientWithSpell.mundane(UItems.GEMSTONE);
    private final SpellType<?> spell;
    private final ItemConvertible gem;
    private final List<IngredientWithSpell> ingredients = new ArrayList<>();
    private SpellTraits traits = SpellTraits.EMPTY;

    public static SpellcraftingRecipeJsonBuilder create(RecipeCategory category, ItemConvertible gem, SpellType<?> spell) {
        return new SpellcraftingRecipeJsonBuilder(category, gem, spell);
    }

    private SpellcraftingRecipeJsonBuilder(RecipeCategory category, ItemConvertible gem, SpellType<?> spell) {
        this.category = category;
        this.gem = gem;
        this.spell = spell;
    }

    public SpellcraftingRecipeJsonBuilder base(ItemConvertible gem, SpellType<?> spell) {
        base = IngredientWithSpell.of(gem, spell);
        return this;
    }

    public SpellcraftingRecipeJsonBuilder input(ItemConvertible gem, SpellType<?> spell) {
        ingredients.add(IngredientWithSpell.of(gem, spell));
        return this;
    }

    public SpellcraftingRecipeJsonBuilder traits(SpellTraits.Builder traits) {
        this.traits = traits.build();
        return this;
    }

    public SpellcraftingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public SpellcraftingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    public void offerTo(RecipeExporter exporter, Identifier id) {
        if (!traits.isEmpty()) {
            criterions.put("has_traits", TraitDiscoveredCriterion.create(traits.stream().map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet())));
        }
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + id);
        Advancement.Builder advancementBuilder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        exporter.accept(id, new SpellCraftingRecipe(base, TraitIngredient.of(traits), ingredients, EnchantableItem.enchant(gem.asItem().getDefaultStack(), spell)), advancementBuilder.build(id.withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    public SpellCraftingRecipe create(IngredientWithSpell material, TraitIngredient traits, List<IngredientWithSpell> ingredients, ItemStack result) {
        return null;
    }

    public void offerTo(RecipeExporter exporter) {
        offerTo(exporter, spell.getId());
    }

    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier recipeId = new Identifier(recipePath);
        if (recipeId.equals(spell.getId())) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, recipeId);
    }
}
