package com.minelittlepony.unicopia.datagen;

import java.util.Map;
import java.util.NoSuchElementException;

import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.VanillaRecipeProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registries;
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

    static Either<ItemConvertible, TagKey<Item>> getMaterial(Item output, String toStrip, String suffex) {
        Identifier id = Registries.ITEM.getId(output).withPath(p -> p.replace(toStrip, "") + suffex);
        TagKey<Item> tag = MATERIALS.getOrDefault(id.getPath().replace("en_", "_").split("_")[0], null);
        if (tag != null) {
            return Either.right(tag);
        }
        return Either.left(
            Registries.ITEM.getOrEmpty(id)
                .or(() -> Registries.ITEM.getOrEmpty(new Identifier(Identifier.DEFAULT_NAMESPACE, id.getPath())))
                .or(() -> Registries.ITEM.getOrEmpty(new Identifier(Identifier.DEFAULT_NAMESPACE, id.getPath().replace(suffex, ""))))
                .orElseThrow(() -> new NoSuchElementException("No item with id " + id))
        );
    }

    static Item getItem(Identifier id) {
        return Registries.ITEM.getOrEmpty(id).orElseThrow(() -> new NoSuchElementException("No item with id " + id));
    }

    static ShapedRecipeJsonBuilder input(ShapedRecipeJsonBuilder builder, char key, Either<ItemConvertible, TagKey<Item>> material) {
        material.ifLeft(i -> builder.input(key, i));
        material.ifRight(i -> builder.input(key, i));
        return builder;
    }

    static InventoryChangedCriterion.Conditions conditionsFromEither(Either<ItemConvertible, TagKey<Item>> material) {
        return material.map(RecipeProvider::conditionsFromItem, RecipeProvider::conditionsFromTag);
    }

    static String hasEither(Either<ItemConvertible, TagKey<Item>> material) {
        return material.map(VanillaRecipeProvider::hasItem, CraftingMaterialHelper::hasTag);
    }

    static String hasTag(TagKey<Item> tag) {
        return "has_" + tag.id();
    }
}
