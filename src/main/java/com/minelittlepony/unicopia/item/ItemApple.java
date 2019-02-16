package com.minelittlepony.unicopia.item;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.edibles.IEdible;
import com.minelittlepony.unicopia.edibles.Toxicity;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.util.collection.Pool;
import com.minelittlepony.util.collection.Weighted;

import net.minecraft.block.BlockPlanks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class ItemApple extends ItemFood implements IEdible {

    public static final Pool<Object, Weighted<Supplier<ItemStack>>> typeVariantMap = Pool.of(BlockPlanks.EnumType.OAK,
            BlockPlanks.EnumType.OAK, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.green_apple))
                    .put(3, () -> new ItemStack(UItems.red_apple)),
            BlockPlanks.EnumType.SPRUCE, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.sour_apple))
                    .put(2, () -> new ItemStack(UItems.green_apple))
                    .put(3, () -> new ItemStack(UItems.sweet_apple))
                    .put(4, () -> new ItemStack(UItems.rotten_apple)),
            BlockPlanks.EnumType.BIRCH, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(5, () -> new ItemStack(UItems.green_apple)),
            BlockPlanks.EnumType.JUNGLE, new Weighted<Supplier<ItemStack>>()
                    .put(5, () -> new ItemStack(UItems.green_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(1, () -> new ItemStack(UItems.sour_apple)),
            BlockPlanks.EnumType.ACACIA, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(5, () -> new ItemStack(UItems.green_apple)),
            BlockPlanks.EnumType.DARK_OAK, new Weighted<Supplier<ItemStack>>()
                    .put(1, () -> new ItemStack(UItems.rotten_apple))
                    .put(2, () -> new ItemStack(UItems.sweet_apple))
                    .put(5, () -> new ItemStack(UItems.zap_apple)
            )
    );

    public static ItemStack getRandomItemStack(Object variant) {
        return typeVariantMap.getOptional(variant)
                .flatMap(Weighted::get)
                .map(Supplier::get)
                .orElse(ItemStack.EMPTY);
    }

    public ItemStack getRandomApple() {
        return getRandomApple(null);
    }

    public ItemStack getRandomApple(Object variant) {
        return getRandomItemStack(variant);
    }

    public ItemApple(String domain, String name) {
        super(4, 3, false);

        setTranslationKey(name);

        if (!"minecraft".contentEquals(domain)) {
            setRegistryName(domain, name);
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this == UItems.red_apple && isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            items.add(new ItemStack(UItems.green_apple));
            items.add(new ItemStack(UItems.sweet_apple));
            items.add(new ItemStack(UItems.sour_apple));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(getToxicityLevel(stack).getTooltip());
    }

    @Override
    public Toxicity getToxicityLevel(ItemStack stack) {
        return Toxicity.SAFE;
    }
}
