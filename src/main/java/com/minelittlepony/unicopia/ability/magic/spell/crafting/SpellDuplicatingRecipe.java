package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.container.SpellbookScreenHandler.SpellbookInventory;
import com.minelittlepony.unicopia.item.*;
import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A recipe for creating a new spell from input traits and items.
 */
public class SpellDuplicatingRecipe implements SpellbookRecipe {
    private final Identifier id;

    private final IngredientWithSpell material;

    private SpellDuplicatingRecipe(Identifier id, IngredientWithSpell material) {
        this.id = id;
        this.material = material;
    }

    @Override
    public void buildCraftingTree(CraftingTreeBuilder builder) {
        ItemStack[] spells = SpellType.REGISTRY.stream()
                .filter(SpellType::isObtainable)
                .map(type -> GemstoneItem.enchant(UItems.GEMSTONE.getDefaultStack(), type))
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
                .noneMatch(i -> !i.isOf(UItems.GEMSTONE) || !GemstoneItem.isEnchanted(i))
                && material.test(stack)
                && !GemstoneItem.isEnchanted(stack);
    }

    @Override
    public ItemStack craft(SpellbookInventory inventory) {
        return InventoryUtil.stream(inventory)
            .filter(i -> i.isOf(UItems.GEMSTONE))
            .filter(GemstoneItem::isEnchanted)
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
    public ItemStack getOutput() {
        ItemStack stack = UItems.GEMSTONE.getDefaultStack();
        stack.setCount(2);
        return stack;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.SPELL_DUPLICATING;
    }

    public static class Serializer implements RecipeSerializer<SpellDuplicatingRecipe> {
        @Override
        public SpellDuplicatingRecipe read(Identifier id, JsonObject json) {
            return new SpellDuplicatingRecipe(id, IngredientWithSpell.fromJson(json.get("material")));
        }

        @Override
        public SpellDuplicatingRecipe read(Identifier id, PacketByteBuf buf) {
            return new SpellDuplicatingRecipe(id, IngredientWithSpell.fromPacket(buf));
        }

        @Override
        public void write(PacketByteBuf buf, SpellDuplicatingRecipe recipe) {
            recipe.material.write(buf);
        }
    }
}
