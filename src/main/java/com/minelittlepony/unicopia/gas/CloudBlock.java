package com.minelittlepony.unicopia.gas;

import java.util.Random;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.unicopia.UBlocks;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.item.MossItem;
import com.minelittlepony.unicopia.util.HoeUtil;

import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CloudBlock extends Block implements Gas, HoeUtil.Tillable {

    private final CloudType variant;

    public CloudBlock(Material material, CloudType variant) {
        super(FabricBlockSettings.of(material)
                .strength(0.5F, 1)
                .sounds(BlockSoundGroup.WOOL)
                .ticksRandomly()
                .build()
        );
        this.variant = variant;
        HoeUtil.registerTillingAction(this, UBlocks.cloud_farmland.getDefaultState());
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return variant == CloudType.NORMAL;
    }

    @Override
    public boolean isOpaque(BlockState state) {
        return variant != CloudType.NORMAL;
    }

    @Override
    public void onScheduledTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(10) == 0) {
            pos = pos.offset(Direction.random(rand), 1 + rand.nextInt(2));
            state = world.getBlockState(pos);

            BlockState converted = MossItem.AFFECTED.getInverse().getConverted(state);

            if (!state.equals(converted)) {
                world.setBlockState(pos, converted);
            }
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return variant == CloudType.NORMAL ? BlockRenderLayer.TRANSLUCENT : super.getRenderLayer();
    }

    @Override
    public boolean isSideInvisible(BlockState state, BlockState beside, Direction face) {
        if (beside.getBlock() instanceof Gas) {
            Gas cloud = ((Gas)beside.getBlock());

            if (cloud.getCloudMaterialType(beside) == getCloudMaterialType(state)) {
                return true;
            }
        }

        return super.isSideInvisible(state, beside, face);
    }

    @Override
    public void onLandedUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!applyLanding(entityIn, fallDistance)) {
            super.onLandedUpon(world, pos, entityIn, fallDistance);
        }
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (!applyRebound(entity)) {
            super.onEntityLand(world, entity);
        }
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
        return variant;
    }

    @Override
    public boolean canTill(ItemUsageContext context) {
        return context.getPlayer() == null || Pony.of(context.getPlayer()).getSpecies().canInteractWithClouds();
    }
}
