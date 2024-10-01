package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.recipe.URecipes;
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
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class SpellShapedCraftingRecipe extends ShapedRecipe {
    public static final MapCodec<SpellShapedCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(SpellShapedCraftingRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(SpellShapedCraftingRecipe::getCategory),
            RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw), ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(SpellShapedCraftingRecipe::showNotification)
    ).apply(instance, SpellShapedCraftingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, SpellShapedCraftingRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SpellShapedCraftingRecipe::getGroup,
            CraftingRecipeCategory.PACKET_CODEC, SpellShapedCraftingRecipe::getCategory,
            RawShapedRecipe.PACKET_CODEC, recipe -> recipe.raw,
            ItemStack.PACKET_CODEC, recipe -> recipe.result,
            PacketCodecs.BOOL, SpellShapedCraftingRecipe::showNotification,
            SpellShapedCraftingRecipe::new
    );

    private final RawShapedRecipe raw;
    private final ItemStack result;

    public SpellShapedCraftingRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.result = result;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        return inventory.getStacks().stream()
            .filter(stack -> stack.getItem() instanceof EnchantableItem)
            .filter(EnchantableItem::isEnchanted)
            .map(stack -> EnchantableItem.getSpellEffect(stack))
            .findFirst()
            .map(spell -> spell.traits().applyTo(EnchantableItem.enchant(super.craft(inventory, registries), spell.type())))
            .orElseGet(() -> super.craft(inventory, registries));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.CRAFTING_MAGICAL_SERIALIZER;
    }
}
