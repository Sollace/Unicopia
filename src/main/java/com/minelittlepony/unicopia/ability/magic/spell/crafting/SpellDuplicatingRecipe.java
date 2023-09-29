package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.container.inventory.SpellbookInventory;
import com.minelittlepony.unicopia.item.*;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.World;

/**
 * A recipe for creating a new spell from input traits and items.
 */
public class SpellDuplicatingRecipe implements SpellbookRecipe {
    final IngredientWithSpell material;

    private SpellDuplicatingRecipe(IngredientWithSpell material) {
        this.material = material;
    }

    @Override
    public void buildCraftingTree(CraftingTreeBuilder builder) {
        ItemStack[] spells = SpellType.REGISTRY.stream()
                .filter(SpellType::isObtainable)
                .map(UItems.GEMSTONE::getDefaultStack)
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
    public boolean matches(SpellbookInventory inventory, World world) {
        ItemStack stack = inventory.getItemToModify();
        return InventoryUtil.stream(inventory)
                .limit(inventory.size() - 1)
                .filter(i -> !i.isEmpty())
                .noneMatch(i -> !i.isOf(UItems.GEMSTONE) || !EnchantableItem.isEnchanted(i))
                && material.test(stack)
                && !EnchantableItem.isEnchanted(stack);
    }

    @Override
    public ItemStack craft(SpellbookInventory inventory, DynamicRegistryManager registries) {
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
    public ItemStack getResult(DynamicRegistryManager registries) {
        ItemStack stack = UItems.GEMSTONE.getDefaultStack();
        stack.setCount(2);
        return stack;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.SPELL_DUPLICATING;
    }

    public static class Serializer implements RecipeSerializer<SpellDuplicatingRecipe> {
        private static final Codec<SpellDuplicatingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                IngredientWithSpell.CODEC.fieldOf("material").forGetter(recipe -> recipe.material)
        ).apply(instance, SpellDuplicatingRecipe::new));

        @Override
        public Codec<SpellDuplicatingRecipe> codec() {
            return CODEC;
        }

        @Override
        public SpellDuplicatingRecipe read(PacketByteBuf buf) {
            return new SpellDuplicatingRecipe(IngredientWithSpell.fromPacket(buf));
        }

        @Override
        public void write(PacketByteBuf buf, SpellDuplicatingRecipe recipe) {
            recipe.material.write(buf);
        }
    }
}
