package com.minelittlepony.unicopia.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.CloudType;
import com.minelittlepony.util.WorldEvent;

import net.minecraft.block.AnvilBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockCloudAnvil extends AnvilBlock implements ICloudBlock {
    public BlockCloudAnvil(String domain, String name) {
        super();

        setSoundType(SoundType.CLOTH);
        setHardness(0.025F);
        setResistance(2000);
        setRegistryName(domain, name);
        setTranslationKey(name);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public String getHarvestTool(BlockState state) {
        return "shovel";
    }

    @Override
    public int getHarvestLevel(BlockState state) {
        return 0;
    }

    @Override
    public void onFallenUpon(World world, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!applyLanding(entityIn, fallDistance)) {
            super.onFallenUpon(world, pos, entityIn, fallDistance);
        }
    }

    @Override
    public void onEndFalling(World world, BlockPos pos, BlockState fallingState, BlockState hitState) {
        WorldEvent.ENTITY_TAKEOFF.play(world, pos);
    }

    @Override
    public void onBroken(World world, BlockPos pos) {
        WorldEvent.ENTITY_TAKEOFF.play(world, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    protected void onStartFalling(EntityFallingBlock fallingEntity) {
        fallingEntity.setHurtEntities(true);
    }

    @Override
    public void onLanded(World worldIn, Entity entity) {
        if (!applyRebound(entity)) {
            super.onLanded(worldIn, entity);
        }
    }

    @Override
    public boolean isAir(BlockState state, BlockView world, BlockPos pos) {
        return allowsFallingBlockToPass(state, world, pos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, BlockState state, Random rand) {
        BlockState below = world.getBlockState(pos.down());

        if (below.getBlock() instanceof ICloudBlock) {
            if (((ICloudBlock)below.getBlock()).isDense(below)) {
                return;
            }
        }

        super.updateTick(world, pos, state, rand);
    }

    @Override
    public void onEntityCollision(World w, BlockPos pos, BlockState state, Entity entity) {
        if (!applyBouncyness(state, entity)) {
            super.onEntityCollision(w, pos, state, entity);
        }
    }

    @Override
    public boolean canHarvestBlock(BlockView world, BlockPos pos, PlayerEntity player) {
        return getCanInteract(world.getBlockState(pos), player);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return getCanInteract(state, entity) && super.canEntityDestroy(state, world, pos, entity);
    }

    @Deprecated
    public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, Box entityBox, List<Box> collidingBoxes, @Nullable Entity entity, boolean p_185477_7_) {
        if (getCanInteract(state, entity)) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, p_185477_7_);
        }
    }

    @Deprecated
    @Override
    public RayTraceResult collisionRayTrace(BlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (!handleRayTraceSpecialCases(worldIn, pos, blockState)) {
            return super.collisionRayTrace(blockState, worldIn, pos, start, end);
        }
        return null;
    }

    @Deprecated
    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, World worldIn, BlockPos pos) {
        if (!CloudType.NORMAL.canInteract(player)) {
            return -1;
        }
        return super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);
    }


    @Override
    public CloudType getCloudMaterialType(BlockState blockState) {
        return CloudType.NORMAL;
    }
}
