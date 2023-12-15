package com.minelittlepony.unicopia.item;

import java.util.HashMap;
import java.util.Map;

import com.minelittlepony.unicopia.block.FancyBedBlock;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BedsheetsItem extends Item {
    private static final Map<CloudBedBlock.SheetPattern, Item> ITEMS = new HashMap<>();

    private final CloudBedBlock.SheetPattern pattern;

    public static Item forPattern(CloudBedBlock.SheetPattern pattern) {
        return ITEMS.getOrDefault(pattern, Items.AIR);
    }

    public BedsheetsItem(CloudBedBlock.SheetPattern pattern, Settings settings) {
        super(settings);
        this.pattern = pattern;
        ITEMS.put(pattern, this);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);

        if (state.getBlock() instanceof FancyBedBlock) {
            FancyBedBlock.setBedPattern(world, context.getBlockPos(), pattern);
            context.getStack().decrement(1);
            PlayerEntity player = context.getPlayer();
            world.playSound(player, pos, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, SoundCategory.BLOCKS, 1, 1);

            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }
}
