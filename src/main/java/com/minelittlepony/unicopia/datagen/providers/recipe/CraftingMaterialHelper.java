package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Map;
import java.util.NoSuchElementException;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.recipe.CloudShapingRecipe;
import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.data.server.recipe.RecipeGenerator;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.StonecuttingRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.predicate.ComponentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface CraftingMaterialHelper {
    Map<String, TagKey<Item>> MATERIALS = Map.of(
            "wood", ItemTags.PLANKS,
            "stone", ItemTags.STONE_TOOL_MATERIALS,
            "iron", ConventionalItemTags.IRON_INGOTS,
            "gold", ConventionalItemTags.GOLD_INGOTS,
            "copper", ConventionalItemTags.COPPER_INGOTS,
            "netherite", ConventionalItemTags.NETHERITE_INGOTS
    );

    default RecipeGenerator asGenerator() {
        return (RecipeGenerator)this;
    }

    default Either<ItemConvertible, TagKey<Item>> getMaterial(Item output, String toStrip, String suffex) {
        Identifier id = Registries.ITEM.getId(output).withPath(p -> p.replace(toStrip, "") + suffex);
        TagKey<Item> tag = MATERIALS.getOrDefault(id.getPath().replace("en_", "_").split("_")[0], null);
        if (tag != null) {
            return Either.right(tag);
        }
        return Either.left(
            Registries.ITEM.getOptionalValue(id)
                .or(() -> Registries.ITEM.getOptionalValue(Identifier.ofVanilla(id.getPath())))
                .or(() -> Registries.ITEM.getOptionalValue(Identifier.ofVanilla(id.getPath().replace(suffex, ""))))
                .orElseThrow(() -> new NoSuchElementException("No item with id " + id))
        );
    }

    default Item getItem(Identifier id) {
        return Registries.ITEM.getOptionalValue(id).orElseThrow(() -> new NoSuchElementException("No item with id " + id));
    }

    default ShapedRecipeJsonBuilder input(ShapedRecipeJsonBuilder builder, char key, Either<ItemConvertible, TagKey<Item>> material) {
        material.ifLeft(i -> builder.input(key, i));
        material.ifRight(i -> builder.input(key, i));
        return builder;
    }

    default AdvancementCriterion<?> conditionsFromEither(Either<ItemConvertible, TagKey<Item>> material) {
        return material.map(asGenerator()::conditionsFromItem, asGenerator()::conditionsFromTag);
    }

    default String hasEither(Either<ItemConvertible, TagKey<Item>> material) {
        return material.map(RecipeGenerator::hasItem, this::hasTag);
    }

    default String hasTag(TagKey<Item> tag) {
        return "has_" + tag.id();
    }

    default AdvancementCriterion<?> conditionsFromSpell(RegistryEntryLookup<Item> items, ItemConvertible gem, SpellType<?> spell) {
        return RecipeGenerator.conditionsFromItemPredicates(ItemPredicate.Builder.create()
                .items(items, gem)
                .component(ComponentPredicate.builder().add(UDataComponentTypes.STORED_SPELL, spell).build())
                .build()
        );
    }

    default String hasSpell(SpellType<?> spell) {
        return "has_" + spell.getId() + "_gemstone";
    }

    default StonecuttingRecipeJsonBuilder createCloudShaping(Ingredient input, RecipeCategory category, ItemConvertible output, int count) {
        return new StonecuttingRecipeJsonBuilder(category, CloudShapingRecipe::new, input, output, count);
    }
}
