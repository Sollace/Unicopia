package com.minelittlepony.unicopia.item;

import java.util.Random;

import com.minelittlepony.unicopia.UItems;

import net.minecraft.block.BlockPlanks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemApple extends ItemFood {

	private int[] typeRarities = new int[0];

	private String[] subTypes = new String[0];

	private String[] variants = subTypes;

	public ItemStack getRandomApple(Random rand, Object variant) {

		int[] rarity = typeRarities;

		int result = 0;

		for (int i = 0; i < rarity.length && i < subTypes.length; i++) {
			if (rand.nextInt(rarity[i]) == 0) {
				result++;
			}
		}

		if (variant == BlockPlanks.EnumType.JUNGLE) {
		    result = oneOr(result, 0, 1);
		}

		if (variant == BlockPlanks.EnumType.BIRCH) {
            result = oneOr(result, 0, 2);
        }

		if (variant == BlockPlanks.EnumType.SPRUCE) {
		    if (result == 0) {
		        return new ItemStack(UItems.rotten_apple, 1);
		    }
		}

		if (variant == BlockPlanks.EnumType.DARK_OAK) {
		    if (result == 1) {
		        return new ItemStack(UItems.zap_apple, 1);
		    }
		}

		if (variant == BlockPlanks.EnumType.ACACIA) {
            result = oneOr(result, 0, 4);
        }

		return new ItemStack(this, 1, result);
	}

	int oneOr(int initial, int a, int b) {
	    if (initial == a) {
	        return b;
	    }

	    if (initial == b) {
	        return a;
	    }

	     return initial;
	}

	public ItemApple(String domain, String name) {
		super(4, 3, false);
		setTranslationKey(name);
		setRegistryName(domain, name);
	}

	public int getZapAppleMetadata() {
		return 4;
	}

	public ItemApple setSubTypes(String... types) {
	    setHasSubtypes(true);
        setMaxDamage(0);

		subTypes = types;
		variants = new String[subTypes.length];

		setTranslationKey(variants[0] = types[0]);

		for (int i = 1; i < variants.length; i++) {
			variants[i] = variants[0] + "_" + subTypes[i % subTypes.length];
		}
		return this;
	}

	public ItemApple setTypeRarities(int ... rarity) {
		typeRarities = rarity;
		return this;
	}

	public String[] getVariants() {
		return variants;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
	    if (isInCreativeTab(tab)) {
	        items.add(new ItemStack(this, 1, 0));

    		for (int i = 1; i < subTypes.length; i++) {
    			items.add(new ItemStack(this, 1, i));
    		}
	    }
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
	    if (subTypes.length > 0) {
    	    int meta = Math.max(0, stack.getMetadata() % subTypes.length);

    		return super.getTranslationKey(stack) + (meta > 0 ? "." + subTypes[meta] : "");
	    }

	    return super.getTranslationKey(stack);
	}
}
