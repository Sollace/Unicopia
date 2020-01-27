package com.minelittlepony.unicopia.redux.item;

import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PredicatedBlockItem extends BlockItem {

    private final Predicate<PlayerEntity> abilityTest;

    public PredicatedBlockItem(Block block, Item.Settings settings, Predicate<PlayerEntity> abilityTest) {
        super(block, settings);

        this.abilityTest = abilityTest;
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        if (context.getPlayer() != null && !(context.getPlayer().abilities.creativeMode || abilityTest.test(context.getPlayer()))) {
            return false;
        }
        return super.canPlace(context, state);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!(player.abilities.creativeMode || abilityTest.test(player))) {
            return false;
        }

        return super.canMine(state, world, pos, player);
    }
}
