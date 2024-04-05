package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.dynamic.Codecs;

public class SpellShapedCraftingRecipe extends ShapedRecipe {
    private final RawShapedRecipe raw;
    private final ItemStack result;

    public SpellShapedCraftingRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe raw, ItemStack result, boolean showNotification) {
        super(group, category, raw, result, showNotification);
        this.raw = raw;
        this.result = result;
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

    public static class Serializer implements RecipeSerializer<SpellShapedCraftingRecipe> {
        public static final Codec<SpellShapedCraftingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(SpellShapedCraftingRecipe::getGroup),
                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(SpellShapedCraftingRecipe::getCategory),
                RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw), ItemStack.RECIPE_RESULT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                Codecs.createStrictOptionalFieldCodec(Codec.BOOL, "show_notification", true).forGetter(SpellShapedCraftingRecipe::showNotification)
        ).apply(instance, SpellShapedCraftingRecipe::new));

        @Override
        public Codec<SpellShapedCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public SpellShapedCraftingRecipe read(PacketByteBuf buffer) {
            return new SpellShapedCraftingRecipe(
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    RawShapedRecipe.readFromBuf(buffer),
                    buffer.readItemStack(),
                    buffer.readBoolean()
            );
        }

        @Override
        public void write(PacketByteBuf packetByteBuf, SpellShapedCraftingRecipe shapedRecipe) {
            packetByteBuf.writeString(shapedRecipe.getGroup());
            packetByteBuf.writeEnumConstant(shapedRecipe.getCategory());
            shapedRecipe.raw.writeToBuf(packetByteBuf);
            packetByteBuf.writeItemStack(shapedRecipe.result);
            packetByteBuf.writeBoolean(shapedRecipe.showNotification());
        }
    }
}
