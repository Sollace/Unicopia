package com.minelittlepony.unicopia.mixin.client;

import java.util.SortedSet;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.render.BlockBreakingInfo;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.resource.SynchronousResourceReloader;

@Mixin(WorldRenderer.class)
abstract class MixinWorldRenderer implements SynchronousResourceReloader, AutoCloseable, ClientBlockDestructionManager.Source {

    private final ClientBlockDestructionManager destructions = new ClientBlockDestructionManager();

    @Shadow
    private @Final Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

    @Override
    public ClientBlockDestructionManager getDestructionManager() {
        return destructions;
    }

    @Override
    @Accessor("ticks")
    public abstract int getTicks();

    @Redirect(method = "render("
            + "Lnet/minecraft/client/util/math/MatrixStack;"
            + "FJZ"
            + "Lnet/minecraft/client/render/Camera;"
            + "Lnet/minecraft/client/render/GameRenderer;"
            + "Lnet/minecraft/client/render/LightmapTextureManager;"
            + "Lnet/minecraft/util/math/Matrix4f;"
            + ")V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;blockBreakingProgressions:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;")
    )
    private Long2ObjectMap<SortedSet<BlockBreakingInfo>> redirectGetDamagesMap(WorldRenderer sender) {
        return destructions.getCombinedDestructions(blockBreakingProgressions);
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        destructions.tick(blockBreakingProgressions);
    }
}
