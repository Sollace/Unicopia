package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.util.serialization.PacketCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;

public class ItemConversionShapedRecipe extends ShapedRecipe {
    public static final MapCodec<ItemConversionShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ItemConversionShapedRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(ItemConversionShapedRecipe::getCategory),
            RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw),
            ItemStack.VALIDATED_CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ItemConversionShapedRecipe::showNotification)
    ).apply(i, ItemConversionShapedRecipe::new));
    public static final PacketCodec<RegistryByteBuf, ItemConversionShapedRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, ItemConversionShapedRecipe::getGroup,
            PacketCodecUtils.ofEnum(CraftingRecipeCategory.class), ItemConversionShapedRecipe::getCategory,
            RawShapedRecipe.PACKET_CODEC, recipe -> recipe.raw,
            ItemStack.PACKET_CODEC, recipe -> recipe.base,
            ItemStack.PACKET_CODEC, recipe -> recipe.result,
            PacketCodecs.BOOL, ItemConversionShapedRecipe::showNotification,
            ItemConversionShapedRecipe::new
    );

    final RawShapedRecipe raw;
    final ItemStack base;
    final ItemStack result;

    public ItemConversionShapedRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack base, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.base = base;
        this.result = result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.CONVERSION_SERIALIZER;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return input.getStacks().stream()
                .filter(i -> {

                    return i.isOf(base.getItem());
                })
                .findFirst()
                .map(base -> base.withItem(result.getItem()))
                .orElseGet(() -> getResult(lookup).copy());
    }
}
