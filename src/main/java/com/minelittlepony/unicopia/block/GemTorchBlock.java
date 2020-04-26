package com.minelittlepony.unicopia.block;

import java.util.Random;

import com.minelittlepony.unicopia.gas.GasState;
import com.minelittlepony.unicopia.gas.Gas;
import com.minelittlepony.unicopia.particles.MagicParticleEffect;
import com.minelittlepony.unicopia.util.PosHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TorchBlock;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class GemTorchBlock extends TorchBlock implements Gas {
    private static final VoxelShape STANDING = createCuboidShape(7, 0, 7, 9, 16, 9);

    public GemTorchBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState().with(Properties.LIT, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.LIT);
    }

    @Override
    public int getTickRate(WorldView worldView) {
        return 2;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        PosHelper.all(pos, p -> world.updateNeighborsAlways(p, this), Direction.values());
    }

    @Override
    public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved) {
            PosHelper.all(pos, p -> world.updateNeighborsAlways(p, this), Direction.values());
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView source, BlockPos pos, EntityContext context) {
        return STANDING;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!state.get(Properties.LIT)) {
            ItemStack held = player.getStackInHand(hand);
            if (!held.isEmpty() && (held.getItem() == Items.FLINT_AND_STEEL || held.getItem() == Items.FIRE_CHARGE)) {

                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.with(Properties.LIT, true));

                if (held.getItem() == Items.FLINT_AND_STEEL) {
                    held.damage(1, player, p -> p.sendToolBreakStatus(hand));
                } else if (!player.abilities.creativeMode) {
                    held.decrement(1);
                }
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random rand) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1;
        double z = pos.getZ() + 0.5;

        Direction facing = getDirection(state);

        if (facing.getAxis().isHorizontal()) {

            double drop = 0.22D;
            double offsetDistance = 0.27D;

            facing = facing.getOpposite();

            x += offsetDistance * facing.getOffsetX();
            y += drop;
            z += offsetDistance * facing.getOffsetZ();
        }

        if (state.get(Properties.LIT)) {
            for (int i = 0; i < 3; i++) {
                world.addParticle(MagicParticleEffect.UNICORN,
                        x - 0.3, y - 0.3, z - 0.3,
                        rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            }
        } else {
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        }
    }

    protected Direction getDirection(BlockState state) {
        return Direction.UP;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.hasRain(pos)) {
            if (state.get(Properties.LIT)) {
                world.playSound(null, pos, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.with(Properties.LIT, false));
            }
        } else {
            if (!state.get(Properties.LIT)) {
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
                world.setBlockState(pos, state.with(Properties.LIT, true));
            }
        }
    }

    @Override
    public GasState getGasState(BlockState blockState) {
        return GasState.ENCHANTED;
    }

    @Override
    public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction side) {
        return side == Direction.DOWN ? state.getWeakRedstonePower(world, pos, side) : 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction side) {
        return state.get(Properties.LIT) ? 12 : 0;
    }

    @Override
    public int getLuminance(BlockState state) {
        return state.get(Properties.LIT) ? super.getLuminance(state) : 0;
    }
}
