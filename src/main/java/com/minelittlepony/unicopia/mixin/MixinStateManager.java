package com.minelittlepony.unicopia.mixin;

import java.util.Map;
import java.util.function.Function;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.server.world.WaterLoggingManager;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.StateManager.Factory;
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
        WaterLoggingManager.<O, S>getInstance().getDefaultState(owner, info);
    }
}

@Mixin(StateManager.Builder.class)
abstract class MixinStateManagerBuilder<O, S extends State<O, S>> implements WaterLoggingManager.StateBuilder {
    @Shadow
    private @Final O owner;

    @Shadow
    private @Final Map<String, Property<?>> namedProperties;

    @Inject(method = "build", at = @At("HEAD"))
    private void build(Function<O, S> defaultStateGetter, Factory<O, S> factory, CallbackInfoReturnable<StateManager<O, S>> info) {
        WaterLoggingManager.<O, S>getInstance().appendProperties(owner, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addIfNotPresent(Property<?> property) {
        if (!namedProperties.containsValue(property)) {
            ((StateManager.Builder<O, S>)(Object)this).add(property);
        }
    }
}

@Mixin(AbstractBlock.AbstractBlockState.class)
abstract class MixinBlockState extends State<Block, BlockState> {
    MixinBlockState() {super(null, null, null);}

    @Shadow
    protected abstract BlockState asBlockState();

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void onGetFluidState(CallbackInfoReturnable<FluidState> info) {
        WaterLoggingManager.<Block, BlockState>getInstance().getFluidState(owner, asBlockState(), info);
    }

    @Inject(method = "getStateForNeighborUpdate", at = @At("RETURN"), cancellable = true)
    private void onGetStateForNeighborUpdate(Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> info) {
        WaterLoggingManager.<Block, BlockState>getInstance().getUpdatedState(world, pos, asBlockState(), info);
    }
}