package com.minelittlepony.unicopia.ability.magic.spell.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.container.inventory.SpellbookInventory;
import com.minelittlepony.unicopia.item.EnchantableItem;
import com.minelittlepony.unicopia.item.URecipes;
import com.minelittlepony.unicopia.util.InventoryUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * A recipe for creating a new spell from input traits and items.
 */
public class SpellCraftingRecipe implements SpellbookRecipe {
    /**
     * The ingredient to modify
     */
    final IngredientWithSpell material;

    /**
     * The required traits
     */
    final TraitIngredient requiredTraits;

    /**
     * Items required for crafting.
     */
    final List<IngredientWithSpell> requiredItems;

    /**
     * The resulting item
     */
    final ItemStackWithSpell output;

    private SpellCraftingRecipe(IngredientWithSpell material, TraitIngredient requiredTraits, List<IngredientWithSpell> requiredItems, ItemStackWithSpell output) {
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
        builder.result(output.toItemStack());
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
        return getResult(registries).copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getResult(DynamicRegistryManager registries) {
        return output.toItemStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.TRAIT_REQUIREMENT;
    }

    record ItemStackWithSpell(ItemStack stack, Optional<SpellType<?>> spell) {
        public static final Codec<ItemStackWithSpell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RecipeCodecs.CRAFTING_RESULT.fieldOf("stack").forGetter(ItemStackWithSpell::stack),
                SpellType.REGISTRY.getCodec().optionalFieldOf("spell").forGetter(ItemStackWithSpell::spell)
        ).apply(instance, ItemStackWithSpell::new));

        public ItemStack toItemStack() {
            return spell.filter(s -> s != SpellType.EMPTY_KEY).map(s -> {
                return EnchantableItem.enchant(stack.copy(), s);
            }).orElse(stack);
        }

        public ItemStackWithSpell(ItemStack stack) {
            this(EnchantableItem.unenchant(stack), Optional.of(EnchantableItem.getSpellKey(stack)));
        }
    }

    public static class Serializer implements RecipeSerializer<SpellCraftingRecipe> {
        private static final Codec<SpellCraftingRecipe> CODEC = RecordCodecBuilder.<SpellCraftingRecipe>create(instance -> instance.group(
                IngredientWithSpell.CODEC.fieldOf("material").forGetter(recipe -> recipe.material),
                TraitIngredient.CODEC.fieldOf("traits").forGetter(recipe -> recipe.requiredTraits),
                IngredientWithSpell.CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> recipe.requiredItems),
                ItemStackWithSpell.CODEC.fieldOf("result").forGetter(recipe -> recipe.output)
        ).apply(instance, SpellCraftingRecipe::new));

        @Override
        public Codec<SpellCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public SpellCraftingRecipe read(PacketByteBuf buf) {
            return new SpellCraftingRecipe(
                    IngredientWithSpell.fromPacket(buf),
                    TraitIngredient.fromPacket(buf),
                    buf.readCollection(DefaultedList::ofSize, IngredientWithSpell::fromPacket),
                    new ItemStackWithSpell(buf.readItemStack())
            );
        }

        @Override
        public void write(PacketByteBuf buf, SpellCraftingRecipe recipe) {
            recipe.material.write(buf);
            recipe.requiredTraits.write(buf);
            buf.writeCollection(recipe.requiredItems, (b, i) -> i.write(b));
            buf.writeItemStack(recipe.output.toItemStack());
        }
    }
}
