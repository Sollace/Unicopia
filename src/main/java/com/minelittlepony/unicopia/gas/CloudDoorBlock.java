package com.minelittlepony.unicopia.gas;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.UMaterials;
import com.minelittlepony.unicopia.block.AbstractDoorBlock;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudDoorBlock extends AbstractDoorBlock implements Gas {

    @SuppressWarnings("deprecation")
    public CloudDoorBlock() {
        super(FabricBlockSettings.of(UMaterials.CLOUD)
                .sounds(BlockSoundGroup.WOOL)
                .hardness(3)
                .resistance(200)
                .breakByTool(net.fabricmc.fabric.api.tools.FabricToolTags.SHOVELS, 0)
                .build());
    }

    @Override
    public boolean activate(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return getCanInteract(state, player) && super.activate(state, worldIn, pos, player, hand, hit);
    }

    @Deprecated
    @Override
    public float getHardness(BlockState blockState, BlockView world, BlockPos pos) {
        float hardness = super.getHardness(blockState, world, pos);

        return Math.max(hardness, Math.min(60, hardness + (pos.getY() - 100)));
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public void onEntityCollision(BlockState state, World w, BlockPos pos, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(state, w, pos, entity);
        }
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView worldIn, BlockPos pos) {
        if (CloudType.NORMAL.canInteract(player)) {
            return super.calcBlockBreakingDelta(state, player, worldIn, pos);
        }
        return -1;
    }

    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return CloudType.NORMAL;
    }
}
