package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.forgebullshit.IMultiItem;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemAppleMultiType extends ItemApple implements IMultiItem {

    private String[] subTypes = new String[0];

    private String[] variants = subTypes;

    public ItemAppleMultiType(String domain, String name) {
        super(domain, name);
    }

    public ItemAppleMultiType setSubTypes(String... types) {
        setHasSubtypes(types.length > 0);
        setMaxDamage(0);

        subTypes = types;
        variants = new String[types.length];

        setTranslationKey(variants[0] = types[0]);

        for (int i = 1; i < variants.length; i++) {
            variants[i] = variants[0] + "_" + types[i % types.length];
        }
        return this;
    }

    @Override
    public String[] getVariants() {
        return variants;
    }

    protected String[] getSubTypes() {
        return subTypes;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0));

            for (int i = 1; i < getSubTypes().length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public int getMetadata(ItemStack stack) {
        if (getHasSubtypes()) {
            return super.getMetadata(stack) % getSubTypes().length;
        }

        return super.getMetadata(stack);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        if (getHasSubtypes()) {
            int meta = Math.max(0, stack.getMetadata() % getSubTypes().length);

            if (meta > 0) {
                return super.getTranslationKey(stack) + "." + getSubTypes()[meta];
            }
        }

        return super.getTranslationKey(stack);
    }
}
