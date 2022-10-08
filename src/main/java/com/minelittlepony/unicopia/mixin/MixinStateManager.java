package com.minelittlepony.unicopia.mixin;

import java.util.Map;
import java.util.function.Function;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.StateManager.Factory;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

@Mixin(StateManager.class)
abstract class MixinStateManager<O, S extends State<O, S>> {
    @Shadow
    private @Final O owner;

    @Inject(method = "getDefaultState", at = @At("RETURN"), cancellable = true)
    private void onGetDefaultState(CallbackInfoReturnable<S> info) {
        if ((owner instanceof SeagrassBlock
                || owner instanceof TallSeagrassBlock
                || owner instanceof KelpBlock
                || owner instanceof KelpPlantBlock
               ) && info.getReturnValue().contains(Properties.WATERLOGGED)) {
            info.setReturnValue(info.getReturnValue().with(Properties.WATERLOGGED, true));
        }
    }
}

@Mixin(StateManager.Builder.class)
abstract class MixinStateManagerBuilder<O, S extends State<O, S>> {
    @Shadow
    private @Final O owner;

    @Shadow
    private @Final Map<String, Property<?>> namedProperties;

    @SuppressWarnings("unchecked")
    @Inject(method = "build", at = @At("HEAD"))
    private void build(Function<O, S> defaultStateGetter, Factory<O, S> factory, CallbackInfoReturnable<StateManager<O, S>> info) {
        if (owner instanceof SeagrassBlock
         || owner instanceof TallSeagrassBlock
         || owner instanceof KelpBlock
         || owner instanceof KelpPlantBlock
        ) {
            if (!namedProperties.containsValue(Properties.WATERLOGGED)) {
                ((StateManager.Builder<O, S>)(Object)this).add(Properties.WATERLOGGED);
            }
        }
    }
}

@Mixin(BlockState.class)
abstract class MixinBlockState extends AbstractBlock.AbstractBlockState {
    protected MixinBlockState() {
        super(null, null, null);
    }

    @Override
    public FluidState getFluidState() {
        if (contains(Properties.WATERLOGGED) && (
                getBlock() instanceof SeagrassBlock
             || getBlock() instanceof TallSeagrassBlock
             || getBlock() instanceof KelpBlock
             || getBlock() instanceof KelpPlantBlock
         )) {
            return (get(Properties.WATERLOGGED) ? Fluids.WATER : Fluids.EMPTY).getDefaultState();
        }
        return super.getFluidState();
    }

    @Override
    public BlockState getStateForNeighborUpdate(Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockState newState = super.getStateForNeighborUpdate(direction, neighborState, world, pos, neighborPos);
        if (newState.isAir()
                && contains(Properties.WATERLOGGED)
                && getBlock() instanceof TallSeagrassBlock
                && contains(TallPlantBlock.HALF)
                && get(TallPlantBlock.HALF) == DoubleBlockHalf.LOWER) {

            BlockState above = world.getBlockState(pos.up());
            if (above.isOf(getBlock())) {
                return asBlockState();
            }
        }
        return newState;
    }
}