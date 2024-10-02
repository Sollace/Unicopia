package com.minelittlepony.unicopia.mixin.client;

import java.util.SortedSet;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.minelittlepony.unicopia.client.ClientBlockDestructionManager;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.server.world.WeatherAccess;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.BlockBreakingInfo;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Precipitation;

@Mixin(value = WorldRenderer.class, priority = 1001)
abstract class MixinWorldRenderer implements SynchronousResourceReloader, AutoCloseable, ClientBlockDestructionManager.Source {

    private final ClientBlockDestructionManager destructions = new ClientBlockDestructionManager();

    @Nullable
    @Shadow
    private ClientWorld world;

    @Shadow
    private @Final Long2ObjectMap<SortedSet<BlockBreakingInfo>> blockBreakingProgressions;

    @Override
    public ClientBlockDestructionManager getDestructionManager() {
        return destructions;
    }

    @Override
    @Accessor("ticks")
    public abstract int getTicks();

    @Redirect(method = "render", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/WorldRenderer;blockBreakingProgressions:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;"
    ))
    private Long2ObjectMap<SortedSet<BlockBreakingInfo>> redirectGetDamagesMap(WorldRenderer sender) {
        return destructions.getCombinedDestructions(blockBreakingProgressions);
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onTick(CallbackInfo info) {
        destructions.tick(blockBreakingProgressions);
    }

    @Inject(method = "renderSky", at = @At(
        value = "INVOKE",
        target = "net/minecraft/client/world/ClientWorld.getSkyAngle(F)F",
        ordinal = 1
    ))
    private void onRenderSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback,
            CallbackInfo info, @Local LocalRef<MatrixStack> matrices) {
        matrices.get().multiply(RotationAxis.POSITIVE_X.rotationDegrees(UnicopiaClient.getInstance().getSkyAngleDelta(tickDelta)));
        matrices.get().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(UnicopiaClient.getInstance().tangentalSkyAngle.getValue()));
    }

    @Redirect(method = "renderWeather", at = @At(
            value = "INVOKE",
            target = "net/minecraft/world/biome/Biome.getPrecipitation(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/biome/Biome$Precipitation;"
    ))
    private Biome.Precipitation modifyPrecipitation(Biome biome, BlockPos pos) {
        Biome.Precipitation precipitation = biome.getPrecipitation(pos);
        if (!((WeatherAccess)world).isBelowClientCloudLayer(pos)) {
            return Precipitation.NONE;
        }
        return precipitation;
    }
}
