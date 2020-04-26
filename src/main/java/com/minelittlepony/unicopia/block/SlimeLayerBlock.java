package com.minelittlepony.unicopia.block;

import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SlimeLayerBlock extends SnowBlock {

    public SlimeLayerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
        Blocks.SLIME_BLOCK.onLandedUpon(world, pos, entity, distance);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        Blocks.SLIME_BLOCK.onEntityLand(world, entity);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, Entity entity) {
        double velocity = Math.abs(entity.getVelocity().y);

        if (velocity < 0.1 && !entity.bypassesSteppingEffects()) {
            double factor = 0.4D + velocity * (0.2D * 8/world.getBlockState(pos).get(LAYERS));

            entity.setVelocity(entity.getVelocity().multiply(factor, 1, factor));
        }
    }
}
