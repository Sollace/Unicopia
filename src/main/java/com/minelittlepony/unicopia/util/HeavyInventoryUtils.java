package com.minelittlepony.unicopia.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.EmptyBlockView;

public class HeavyInventoryUtils {

    public static double getContentsTotalWorth(Inventory inventory, boolean deep) {
        double total = 0;

        for (int i = 0; i < inventory.getInvSize(); i++) {
            ItemStack stack = inventory.getInvStack(i);

            double weightOfOne = decodeStackWeight(stack, deep);

            if (weightOfOne == 0) {
                total += stack.getCount();
            } else {
                total += weightOfOne * stack.getCount();
            }
        }

        return total;
    }

    public static void encodeStackWeight(ItemStack stack, double weight, boolean deep) {
        CompoundTag compound = stack.getSubTag("inventory");
        if (weight == 0 && compound != null) {
            compound.remove("weight");
            compound.remove("deep");
            if (compound.isEmpty()) {
                stack.removeSubTag("inventory");
            }
        } else {
            if (weight != 0) {
                if (compound == null) {
                    compound = stack.getOrCreateSubTag("inventory");
                }

                compound.putDouble("weight", weight);
                if (deep) {
                    compound.putBoolean("deep", deep);
                }
            }
        }
    }

    public static double decodeStackWeight(ItemStack stack, boolean deep) {
        if (!stack.isEmpty() && stack.hasTag()) {
            CompoundTag bet = stack.getSubTag("BlockEntityTag");
            CompoundTag compound = stack.getSubTag("inventory");

            boolean hasWeight = compound != null && compound.containsKey("weight");

            if (deep) {
                if (!hasWeight && bet != null) {
                    Block b = Block.getBlockFromItem(stack.getItem());
                    BlockEntity te = b.hasBlockEntity() ? ((BlockEntityProvider)b).createBlockEntity(EmptyBlockView.INSTANCE) : null;

                    double weight = 0;

                    if (te instanceof Inventory) {
                        te.fromTag(bet);

                        weight = getContentsTotalWorth((Inventory)te, deep);
                    }

                    encodeStackWeight(stack, weight, deep);

                    return weight;
                }
            }

            if (hasWeight && (deep || !compound.containsKey("deep"))) {
                return compound.getDouble("weight");
            }
        }

        return 0;
    }
}
