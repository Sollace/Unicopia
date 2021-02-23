package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.BlockDestructionManager;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;

@Mixin(ServerWorld.class)
abstract class MixinServerWorld extends World implements StructureWorldAccess {
    private MixinServerWorld() {super(null, null, null, null, false, false, 0);}

    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    private void onOnBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo info) {
        ((BlockDestructionManager.Source)this).getDestructionManager().onBlockChanged(pos, oldState, newState);
    }
}
