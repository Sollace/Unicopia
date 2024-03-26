package com.minelittlepony.unicopia.mixin;

import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minelittlepony.unicopia.entity.duck.RotatedView;
import com.minelittlepony.unicopia.server.world.BlockDestructionManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(World.class)
abstract class MixinWorld implements WorldAccess, BlockDestructionManager.Source, RotatedView {

    private final Supplier<BlockDestructionManager> destructions = BlockDestructionManager.create((World)(Object)this);

    private boolean mirrorEntityStatuses;

    @Override
    public void setMirrorEntityStatuses(boolean enable) {
        mirrorEntityStatuses = enable;
    }

    @Override
    public BlockDestructionManager getDestructionManager() {
        return destructions.get();
    }

    @Inject(method = "sendEntityStatus(Lnet/minecraft/entity/Entity;B)V", at = @At("HEAD"))
    private void onSendEntityStatus(Entity entity, byte status, CallbackInfo info) {
        if (mirrorEntityStatuses) {
            entity.handleStatus(status);
        }
    }
}

