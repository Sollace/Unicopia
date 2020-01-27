package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DiamondDoorBlock extends AbstractDoorBlock {
    public DiamondDoorBlock() {
        super(FabricBlockSettings.of(Material.METAL)
                .sounds(BlockSoundGroup.METAL)
                .materialColor(MaterialColor.DIAMOND)
                .hardness(5)
                .build());
    }

    @Override
    protected boolean canOpen(@Nullable PlayerEntity player) {
        return EquinePredicates.MAGI.test(player);
    }

    @Override
    protected boolean onPowerStateChanged(World world, BlockState state, BlockPos pos, boolean powered) {
        if (state.get(OPEN)) {
            world.setBlockState(pos, state.with(OPEN, false), 2);

            return true;
        }

        return false;
    }
}
