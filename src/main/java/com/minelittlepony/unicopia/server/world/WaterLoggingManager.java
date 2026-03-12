package com.minelittlepony.unicopia.server.world;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.State;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class WaterLoggingManager<O, S extends State<O, S>> {
    private static final WaterLoggingManager<?, ?> INSTANCE = new WaterLoggingManager<>();

    @SuppressWarnings("unchecked")
    public static <O, S extends State<O, S>> WaterLoggingManager<O, S> getInstance() {
        return (WaterLoggingManager<O, S>)INSTANCE;
    }

    private final boolean enabled;

    public WaterLoggingManager() {
        enabled = !Unicopia.getConfig().disableWaterPlantsFix.get();
    }

    public void appendProperties(O owner, StateBuilder builder) {
        if (appliesTo(owner)) {
            builder.addIfNotPresent(Properties.WATERLOGGED);
        }
    }

    public S getDefaultState(O owner, S defaultState) {
        if (appliesTo(owner, defaultState)) {
            return defaultState.with(Properties.WATERLOGGED, !(owner instanceof BedBlock));
        }
        return defaultState;
    }

    @SuppressWarnings("unchecked")
    public BlockState getPlacementState(BlockState state, ItemPlacementContext context) {
        if (state != null && appliesTo((O)state.getBlock(), (S)state)) {
            return state.with(Properties.WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).isIn(FluidTags.WATER));
        }
        return state;
    }

    public void getFluidState(O owner, S state, CallbackInfoReturnable<FluidState> info) {
        if (appliesTo(owner, state)) {
            info.setReturnValue((state.get(Properties.WATERLOGGED) ? Fluids.WATER : Fluids.EMPTY).getDefaultState());
        }
    }

    public BlockState getUpdatedState(WorldView world, BlockPos pos, BlockState oldState, BlockState newState) {
        if (shouldPreventRemoval(world, pos, oldState, newState)) {
            return oldState;
        }
        return newState;
    }

    public boolean appliesTo(O block, S state) {
        return appliesTo(block) && state.contains(Properties.WATERLOGGED);
    }

    public boolean appliesTo(O block) {
        return enabled
            && (block instanceof SeagrassBlock
                || block instanceof TallSeagrassBlock
                || block instanceof KelpBlock
                || block instanceof KelpPlantBlock
                || block instanceof BedBlock);
    }

    public boolean shouldPreventRemoval(WorldView world, BlockPos pos, AbstractBlock.AbstractBlockState oldState, AbstractBlock.AbstractBlockState newState) {
        return enabled
                && newState.isAir()
                && oldState.contains(Properties.WATERLOGGED)
                && oldState.getBlock() instanceof TallSeagrassBlock
                && oldState.contains(TallPlantBlock.HALF)
                && oldState.get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER
                && world.getBlockState(pos.up()).isOf(oldState.getBlock());
    }

    public interface StateBuilder {
        void addIfNotPresent(Property<?> property);
    }
}
