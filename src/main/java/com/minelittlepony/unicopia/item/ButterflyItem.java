package com.minelittlepony.unicopia.item;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.mob.ButterflyEntity;
import com.minelittlepony.unicopia.item.group.MultiItem;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class ButterflyItem extends Item implements MultiItem {

    public ButterflyItem(Settings settings) {
        super(settings);
    }

    @Override
    public List<ItemStack> getDefaultStacks() {
        return Arrays.stream(ButterflyEntity.Variant.VALUES).map(variant -> setVariant(getDefaultStack(), variant)).toList();
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal(getVariant(stack).name()).formatted(Formatting.LIGHT_PURPLE));
    }

    public static ButterflyEntity.Variant getVariant(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("variant", NbtElement.STRING_TYPE)) {
            return ButterflyEntity.Variant.BUTTERFLY;
        }
        String variant = nbt.getString("variant");
        return ButterflyEntity.Variant.byName(variant);
    }

    public static ItemStack setVariant(ItemStack stack, ButterflyEntity.Variant variant) {
        if (stack.isOf(UItems.BUTTERFLY)) {
            stack.getOrCreateNbt().putString("variant", variant.name().toLowerCase(Locale.ROOT));
        }
        return stack;
    }

}
