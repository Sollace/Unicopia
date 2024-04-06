package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.inventory.SpellbookInventory;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.datafixers.util.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * A recipe for creating a new spell from input traits and items.
 */
public class SpellCraftingRecipe implements SpellbookRecipe {
    private final Identifier id;

    /**
     * The ingredient to modify
     */
    private final IngredientWithSpell material;

    /**
     * The required traits
     */
    private final TraitIngredient requiredTraits;

    /**
     * Items required for crafting.
     */
    private final List<IngredientWithSpell> requiredItems;

    /**
     * The resulting item
     */
    private final ItemStack output;

    private SpellCraftingRecipe(Identifier id, IngredientWithSpell material, TraitIngredient requiredTraits, List<IngredientWithSpell> requiredItems, ItemStack output) {
        this.id = id;
        this.material = material;
        this.requiredTraits = requiredTraits;
        this.requiredItems = requiredItems;
        this.output = output;
    }

    @Override
    public void buildCraftingTree(CraftingTreeBuilder builder) {
        builder.input(material.getMatchingStacks());
        for (var ingredient : requiredItems) {
            builder.input(ingredient.getMatchingStacks());
        }
        requiredTraits.min().ifPresent(min -> {
            min.forEach(e -> builder.input(e.getKey(), e.getValue()));
        });
        builder.result(output);
    }

    @Override
    public int getPriority() {
        return requiredItems.isEmpty() ? 0 : -1;
    }

    @Override
    public boolean matches(SpellbookInventory inventory, World world) {

        if (!material.test(inventory.getItemToModify())) {
            return false;
        }

        if (requiredItems.isEmpty()) {
            return requiredTraits.test(inventory.getTraits());
        }

        var outstandingRequirements = new ArrayList<>(requiredItems);
        var ingredients = InventoryUtil.slots(inventory)
                .filter(slot -> !inventory.getStack(slot).isEmpty())
                .map(slot -> Pair.of(slot, inventory.getStack(slot)))
                .collect(Collectors.toList());

        outstandingRequirements.removeIf(requirement -> {
            var found = ingredients.stream().filter(pair -> requirement.test(pair.getSecond())).findAny();
            found.ifPresent(ingredients::remove);
            return found.isPresent();
        });

        if (!outstandingRequirements.isEmpty()) {
            return false;
        }

        return requiredTraits.test(SpellTraits.union(
            ingredients.stream().map(pair -> SpellTraits.of(pair.getSecond()).multiply(pair.getFirst())).toArray(SpellTraits[]::new)
        ));
    }

    @Override
    public ItemStack craft(SpellbookInventory inventory, DynamicRegistryManager registries) {
        return getOutput(registries).copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registries) {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.TRAIT_REQUIREMENT;
    }

    public static ItemStack outputFromJson(JsonObject json) {
        ItemStack stack = ShapedRecipe.outputFromJson(json);
        SpellTraits.fromJson(JsonHelper.getObject(json, "traits", new JsonObject()))
            .map(traits -> traits.applyTo(stack)).orElse(stack);

        SpellType<?> spell = SpellType.getKey(Identifier.tryParse(JsonHelper.getString(json, "spell", "")));
        if (spell != SpellType.EMPTY_KEY) {
            return EnchantableItem.enchant(stack, spell);
        }
        return stack;
    }

    public static class Serializer implements RecipeSerializer<SpellCraftingRecipe> {
        @Override
        public SpellCraftingRecipe read(Identifier id, JsonObject json) {
            return new SpellCraftingRecipe(id,
                    IngredientWithSpell.fromJson(json.get("material")),
                    TraitIngredient.fromJson(JsonHelper.getObject(json, "traits")),
                    IngredientWithSpell.fromJson(JsonHelper.asArray(json.get("ingredients"), "ingredients")),
                    outputFromJson(JsonHelper.getObject(json, "result")));
        }

        @Override
        public SpellCraftingRecipe read(Identifier id, PacketByteBuf buf) {
            return new SpellCraftingRecipe(id,
                    IngredientWithSpell.fromPacket(buf),
                    TraitIngredient.fromPacket(buf),
                    buf.readCollection(DefaultedList::ofSize, IngredientWithSpell::fromPacket),
                    buf.readItemStack()
            );
        }

        @Override
        public void write(PacketByteBuf buf, SpellCraftingRecipe recipe) {
            recipe.material.write(buf);
            recipe.requiredTraits.write(buf);
            buf.writeCollection(recipe.requiredItems, (b, i) -> i.write(b));
            buf.writeItemStack(recipe.output);
        }
    }
}
