package com.minelittlepony.unicopia.item;

import java.util.Arrays;
import java.util.List;
import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.item.group.MultiItem;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ButterflyItem extends Item implements MultiItem {

    public ButterflyItem(Settings settings) {
        super(settings);
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return Arrays.stream(ButterflyEntity.Variant.VALUES).map(variant -> setVariant(getDefaultStack(), variant)).toList();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal(getVariant(stack).name()).formatted(Formatting.LIGHT_PURPLE));
    }

    public static ButterflyEntity.Variant getVariant(ItemStack stack) {
        return stack.getOrDefault(UDataComponentTypes.BUTTERFLY_VARIANT, ButterflyEntity.Variant.BUTTERFLY);
    }

    public static ItemStack setVariant(ItemStack stack, ButterflyEntity.Variant variant) {
        if (stack.isOf(UItems.BUTTERFLY)) {
            stack.set(UDataComponentTypes.BUTTERFLY_VARIANT, variant);
        }
        return stack;
    }

}
