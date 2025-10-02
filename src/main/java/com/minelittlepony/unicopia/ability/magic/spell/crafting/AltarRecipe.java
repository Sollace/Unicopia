package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.List;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public record AltarRecipe(String group, DefaultedList<Ingredient> ingredients, ItemStack result) implements Recipe<AltarRecipe.Input> {
    public static final MapCodec<AltarRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("group").forGetter(AltarRecipe::group),
            CodecUtils.defaultedList(Ingredient.DISALLOW_EMPTY_CODEC, Ingredient.empty()).fieldOf("ingredients").forGetter(AltarRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(AltarRecipe::result)
    ).apply(instance, AltarRecipe::new));
    public static final PacketCodec<RegistryByteBuf, AltarRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AltarRecipe::group,
            Ingredient.PACKET_CODEC.collect(PacketCodecs.toCollection(DefaultedList::ofSize)), AltarRecipe::ingredients,
            ItemStack.PACKET_CODEC, AltarRecipe::result,
            AltarRecipe::new
    );

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.ALTAR_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return URecipes.ALTAR;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean matches(Input input, World world) {
        return input.matcher.match(this, null);
    }

    @Override
    public ItemStack getResult(WrapperLookup registriesLookup) {
        return result;
    }

    @Override
    public ItemStack craft(Input input, WrapperLookup lookup) {
        var match = toMatch(input, lookup);
        match.consumeInputs(match.getMinCrafts());
        return match.getResult();
    }

    public AltarRecipeMatch toMatch(Input input, WrapperLookup lookup) {
        return new AltarRecipeMatch(input.getFilteredInputs(this), result);
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    public static class Input implements RecipeInput {
        private final List<ItemEntity> stacks;
        private final RecipeMatcher matcher = new RecipeMatcher();

        public Input(List<ItemEntity> stacks) {
            this.stacks = stacks;
            stacks.forEach(stack -> matcher.addInput(stack.getStack()));
        }

        public Stream<ItemEntity> getFilteredInputs(Recipe<?> recipe) {
            return stacks.stream().filter(stack -> {
                return recipe.getIngredients().stream().anyMatch(i -> i.test(stack.getStack()));
            });
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stacks.get(slot).getStack();
        }

        @Override
        public int getSize() {
            return stacks.size();
        }
    }
}
