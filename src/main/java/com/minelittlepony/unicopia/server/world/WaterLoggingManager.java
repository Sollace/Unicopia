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
import net.minecraft.world.WorldAccess;

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

    public void getDefaultState(O owner, CallbackInfoReturnable<S> info) {
        if (owner instanceof BedBlock) {
            return;
        }
        if (appliesTo(owner, info.getReturnValue())) {
            info.setReturnValue(info.getReturnValue().with(Properties.WATERLOGGED, true));
        }
    }

    @SuppressWarnings("unchecked")
    public void getPlacementState(ItemPlacementContext context, CallbackInfoReturnable<BlockState> info) {
        BlockState state = info.getReturnValue();
        if (state != null && appliesTo((O)state.getBlock(), (S)state)) {
            info.setReturnValue(state.with(Properties.WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).isIn(FluidTags.WATER)));
        }
    }

    public void getFluidState(O owner, S state, CallbackInfoReturnable<FluidState> info) {
        if (appliesTo(owner, state)) {
            info.setReturnValue((state.get(Properties.WATERLOGGED) ? Fluids.WATER : Fluids.EMPTY).getDefaultState());
        }
    }

    public void getUpdatedState(WorldAccess world, BlockPos pos, BlockState oldState, CallbackInfoReturnable<BlockState> info) {
        if (shouldPreventRemoval(world, pos, oldState, info.getReturnValue())) {
            info.setReturnValue(oldState);
        }
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

    public boolean shouldPreventRemoval(WorldAccess world, BlockPos pos, AbstractBlock.AbstractBlockState oldState, AbstractBlock.AbstractBlockState newState) {
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
