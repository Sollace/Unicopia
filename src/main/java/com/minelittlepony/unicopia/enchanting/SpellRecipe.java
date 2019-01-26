package com.minelittlepony.unicopia.enchanting;

import java.util.ArrayList;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.UItems;
import com.minelittlepony.unicopia.inventory.InventorySpellBook;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

public class SpellRecipe extends Impl<IRecipe> implements IRecipe {

	private String spellId;

	private final NonNullList<RecipeItem> ingredients;

	public static IRecipe deserialize(JsonObject json) {

	    NonNullList<RecipeItem> ingredients = NonNullList.create();

	    for (JsonElement i : json.get("ingredients").getAsJsonArray()) {
	        JsonObject o = i.getAsJsonObject();

            Item item = o.has("item") ? Item.getByNameOrId(o.get("item").getAsString()) : null;



            if (item != null) {
                int metadata = Math.max(0, o.has("data") ? o.get("data").getAsInt() : 0);
                int size = Math.max(1, o.has("count") ? o.get("count").getAsInt() : 1);
                String spell = o.has("spell") ? o.get("spell").getAsString() : null;

                ItemStack stack = new ItemStack(item, size, metadata);

                if (spell != null) {
                    stack = SpellRegistry.instance().enchantStack(stack, spell);
                }

                ingredients.add(new RecipeItem(stack, !o.has("data")));
            }
	    }

	    json = json.get("result").getAsJsonObject();

	    String spellId = json.get("spell").getAsString();

	    return new SpellRecipe(spellId, ingredients);
	}

	public SpellRecipe(String spellName, NonNullList<RecipeItem> ingredients) {
		spellId = spellName;
		this.ingredients = ingredients;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemStack enchantedStack = ((InventorySpellBook)inv).getCraftResultMatrix().getStackInSlot(0);

		if (enchantedStack.isEmpty()) {
		    return false;
		}

		int materialMult = enchantedStack.getCount();

		ArrayList<RecipeItem> toMatch = Lists.newArrayList(ingredients);

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);

			if (!stack.isEmpty()) {
    			if (toMatch.isEmpty() && !stack.isEmpty()) {
    				return false;
    			}

    			if (!toMatch.isEmpty() && !toMatch.removeIf(s -> s.matches(stack, materialMult))) {
    			    return false;
    			}
			}
		}
		return toMatch.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return getRecipeOutput();
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height < ingredients.size();
	}

	@Override
	public ItemStack getRecipeOutput() {
		return SpellRegistry.instance().enchantStack(new ItemStack(UItems.spell, 1), spellId);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> remainers = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < remainers.size(); i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack != null && stack.getItem().hasContainerItem(stack)) {
                remainers.set(i, new ItemStack(stack.getItem().getContainerItem()));
            }
        }
        return remainers;
	}

	private static class RecipeItem {

		private final ItemStack contained;
		private final boolean ignoreMeta;

		RecipeItem(ItemStack stack, boolean meta) {
			contained = stack;
			ignoreMeta = meta;
		}

		boolean matches(ItemStack other,  int materialMult) {
			if (other.isEmpty() != contained.isEmpty()) {
			    return false;
			} else if (other.isEmpty()) {
			    return true;
			}

			if (other.isEmpty()) {
			    return false;
			}

			if (contained.getItem() == other.getItem()
			        && (ignoreMeta || other.getMetadata() == contained.getMetadata())
			        && ItemStack.areItemStackTagsEqual(contained, other)) {
				return other.getCount() >= (materialMult * contained.getCount());
			}

			return false;
		}
	}
}
