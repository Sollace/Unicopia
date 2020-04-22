package com.minelittlepony.unicopia.item;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.blockstate.StateMaps;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.toxin.ToxicItem;
import com.minelittlepony.unicopia.toxin.Toxicity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MossItem extends ToxicItem {

    public MossItem(Item.Settings settings) {
        super(settings, 2, 1, UseAction.EAT, Toxicity.FAIR);
    }

    public boolean tryConvert(World world, BlockState state, BlockPos pos, @Nullable PlayerEntity player) {
        BlockState converted = StateMaps.MOSS_AFFECTED.getConverted(state);

        if (!state.equals(converted)) {
            world.setBlockState(pos, converted, 3);

            world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1, 1);

            int amount = 1;

            if (player != null && Pony.of(player).getSpecies().canUseEarth()) {
                amount = world.random.nextInt(4);
            }

            Block.dropStack(world, pos, new ItemStack(this, amount));

            return true;
        }

        return false;
    }

    @Override
    public Toxicity getToxicity(ItemStack stack) {
        return Toxicity.MILD;
    }
}
