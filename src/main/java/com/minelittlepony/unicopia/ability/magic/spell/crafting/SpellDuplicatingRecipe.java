package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.item.*;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

/**
 * A recipe for creating a new spell from input traits and items.
 */
public record SpellDuplicatingRecipe (IngredientWithSpell material) implements SpellbookRecipe {
    public static final MapCodec<SpellDuplicatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IngredientWithSpell.CODEC.fieldOf("material").forGetter(recipe -> recipe.material)
    ).apply(instance, SpellDuplicatingRecipe::new));
    public static final PacketCodec<RegistryByteBuf, SpellDuplicatingRecipe> PACKET_CODEC = IngredientWithSpell.PACKET_CODEC.xmap(SpellDuplicatingRecipe::new, SpellDuplicatingRecipe::material);

    @Override
    public void buildCraftingTree(CraftingTreeBuilder builder) {
        ItemStack[] spells = SpellType.REGISTRY.stream()
                .filter(SpellType::isObtainable)
                .map(i -> EnchantableItem.enchant(UItems.GEMSTONE.getDefaultStack(), i))
                .toArray(ItemStack[]::new);
        builder.input(UItems.BOTCHED_GEM.getDefaultStack());
        builder.input(spells);
        builder.result(spells);
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean matches(Input inventory, World world) {
        ItemStack stack = inventory.stackToModify();
        return InventoryUtil.stream(inventory)
                .limit(inventory.getSize() - 1)
                .filter(i -> !i.isEmpty())
                .noneMatch(i -> !i.isOf(UItems.GEMSTONE) || !EnchantableItem.isEnchanted(i))
                && material.test(stack)
                && !EnchantableItem.isEnchanted(stack);
    }

    @Override
    public ItemStack craft(Input inventory, WrapperLookup registries) {
        return InventoryUtil.stream(inventory)
            .filter(i -> i.isOf(UItems.GEMSTONE))
            .filter(EnchantableItem::isEnchanted)
            .map(stack -> stack.copy())
            .map(stack -> {
                stack.setCount(2);
                return stack;
            })
            .findFirst().get();
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getResult(WrapperLookup registries) {
        ItemStack stack = UItems.GEMSTONE.getDefaultStack();
        stack.setCount(2);
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.SPELL_DUPLICATING;
    }
}
