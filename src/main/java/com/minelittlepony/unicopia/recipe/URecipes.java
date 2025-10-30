package com.minelittlepony.unicopia.recipe;

import java.util.List;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.*;
import com.minelittlepony.unicopia.datagen.providers.recipe.CuttingBoardRecipeJsonBuilder;
import com.minelittlepony.unicopia.server.world.gen.ULootTableEntryType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface URecipes {
    Codec<List<Ingredient>> SHAPELESS_RECIPE_INGREDIENTS_CODEC = Ingredient.CODEC.listOf(1, 9);

    RecipeType<SpellbookRecipe> SPELLBOOK = register("spellbook");
    RecipeType<CloudShapingRecipe> CLOUD_SHAPING = register("cloud_shaping");
    RecipeType<TransformCropsRecipe> GROWING = register("growing");
    RecipeType<AltarRecipe> ALTAR = register("altar");

    RecipeSerializer<ZapAppleRecipe> ZAP_APPLE_SERIALIZER = register("crafting_zap_apple", ZapAppleRecipe.CODEC, ZapAppleRecipe.PACKET_CODEC);
    RecipeSerializer<ItemConversionShapedRecipe> CONVERSION_SERIALIZER = register("conversion", ItemConversionShapedRecipe.CODEC, ItemConversionShapedRecipe.PACKET_CODEC);
    RecipeSerializer<GlowingRecipe> GLOWING_SERIALIZER = register("crafting_glowing", new SpecialCraftingRecipe.SpecialRecipeSerializer<>(GlowingRecipe::new));
    RecipeSerializer<JarInsertRecipe> JAR_INSERT_SERIALIZER = register("jar_insert", new SpecialCraftingRecipe.SpecialRecipeSerializer<>(JarInsertRecipe::new));
    RecipeSerializer<JarExtractRecipe> JAR_EXTRACT_SERIALIZER = register("jar_extract", new SpecialCraftingRecipe.SpecialRecipeSerializer<>(JarExtractRecipe::new));
    RecipeSerializer<SpellShapedCraftingRecipe> CRAFTING_MAGICAL_SERIALIZER = register("crafting_magical", SpellShapedCraftingRecipe.CODEC, SpellShapedCraftingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellCraftingRecipe> TRAIT_REQUIREMENT = register("spellbook/crafting", SpellCraftingRecipe.CODEC, SpellCraftingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellEnhancingRecipe> TRAIT_COMBINING = register("spellbook/combining", SpellEnhancingRecipe.CODEC, SpellEnhancingRecipe.PACKET_CODEC);
    RecipeSerializer<SpellDuplicatingRecipe> SPELL_DUPLICATING = register("spellbook/duplicating", SpellDuplicatingRecipe.CODEC, SpellDuplicatingRecipe.PACKET_CODEC);
    RecipeSerializer<CloudShapingRecipe> CLOUD_SHAPING_SERIALIZER = register("cloud_shaping", new StonecuttingRecipe.Serializer<>(CloudShapingRecipe::new) {});
    RecipeSerializer<TransformCropsRecipe> TRANSFORM_CROP_SERIALIZER = register("transform_crop", TransformCropsRecipe.CODEC, TransformCropsRecipe.PACKET_CODEC);
    RecipeSerializer<AltarRecipe> ALTAR_SERIALIZER = register("altar", AltarRecipe.CODEC, AltarRecipe.PACKET_CODEC);

    CustomIngredientSerializer<ExclusiveIngredient> EXCLUSIVE_INGREDIENT_SERIALIZER = registerIngredient("exclusive", ExclusiveIngredient.CODEC, ExclusiveIngredient.PACKET_CODEC);

    static <T extends Recipe<?>> RecipeType<T> register(String name) {
        Identifier id = Unicopia.id(name);
        return Registry.register(Registries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    static <T extends Recipe<?>> RecipeSerializer<T> register(String name, MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec) {
        return register(name, new RecipeSerializer<>() {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public PacketCodec<RegistryByteBuf, T> packetCodec() {
                return packetCodec;
            }
        });
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String name, S serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Unicopia.id(name), serializer);
    }

    static <T extends CustomIngredient> CustomIngredientSerializer<T> registerIngredient(String name, MapCodec<T> codec, PacketCodec<RegistryByteBuf, T> packetCodec) {
        var serializer = new CustomIngredientSerializer<T>() {
            private final Identifier id = Unicopia.id(name);

            @Override
            public Identifier getIdentifier() {
                return id;
            }

            @Override
            public MapCodec<T> getCodec() {
                return codec;
            }

            @Override
            public PacketCodec<RegistryByteBuf, T> getPacketCodec() {
                return packetCodec;
            }
        };
        CustomIngredientSerializer.register(serializer);
        return serializer;
    }

    static void bootstrap() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            CuttingBoardRecipeJsonBuilder.CuttingBoardRecipe.bootstrap();
        }
        ULootTableEntryType.bootstrap();
    }
}