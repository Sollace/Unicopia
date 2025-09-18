package com.minelittlepony.unicopia.mixin;

import java.util.Map;
import java.util.function.Function;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.server.world.WaterLoggingManager;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.StateManager.Factory;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

@Mixin(StateManager.class)
abstract class MixinStateManager<O, S extends State<O, S>> {
    @Shadow
    private @Final O owner;

    @ModifyReturnValue(method = "getDefaultState", at = @At("RETURN"))
    private S onGetDefaultState(S state) {
        return WaterLoggingManager.<O, S>getInstance().getDefaultState(owner, state);
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

    @ModifyReturnValue(method = "getStateForNeighborUpdate", at = @At("RETURN"))
    private BlockState onGetStateForNeighborUpdate(BlockState updatedState, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        return WaterLoggingManager.getInstance().getUpdatedState(world, pos, asBlockState(), updatedState);
    }
}
