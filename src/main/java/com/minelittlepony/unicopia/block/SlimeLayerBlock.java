package com.minelittlepony.unicopia.block;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SlimeLayerBlock extends SnowBlock {

    public SlimeLayerBlock() {
        super(FabricBlockSettings.of(Material.CLAY)
                .sounds(BlockSoundGroup.SLIME)
                .materialColor(MaterialColor.GRASS)
                .build()
        );

        // TODO: drops Items.SLIME_BALL x1
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, Entity entity) {
        float factor = getMotionFactor(world.getBlockState(pos));

        entity.setVelocity(entity.getVelocity().multiply(factor));
    }

    protected float getMotionFactor(BlockState state) {
        return 1/state.get(LAYERS);
    }
}
