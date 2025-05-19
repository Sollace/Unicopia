package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.server.world.LightSources;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightStorage;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Mixin(ChunkBlockLightProvider.class)
abstract class MixinChunkBlockLightProvider extends ChunkLightProvider {
    @Shadow
    private @Final BlockPos.Mutable mutablePos;

    MixinChunkBlockLightProvider() { super(null, null); }

    @Inject(method = "propagateLight", at = @At("TAIL"))
    private void onPropagateLight(ChunkPos chunkPos, CallbackInfo info) {
        if (chunkProvider.getChunk(chunkPos.x, chunkPos.z) instanceof WorldChunk chunk && chunk.getWorld() instanceof ServerWorld world) {
            LightSources.get(world).forEachLightSource(chunkPos, (pos, level) -> {
                method_51566(pos.asLong(), ChunkLightProvider.class_8531.method_51573(level, false));
            });
        }
    }

    @Inject(method = "method_51529", at = @At("TAIL"))
    private void onMethod_51529(long blockPos, CallbackInfo info) {
        long sectionPos = ChunkSectionPos.fromBlockPos(blockPos);
        if (!((MutableBlockLightStorage)lightStorage).invokeHasSection(sectionPos)) {
            return;
        }

        int x = ChunkSectionPos.getSectionCoord(BlockPos.unpackLongX(blockPos));
        int z = ChunkSectionPos.getSectionCoord(BlockPos.unpackLongZ(blockPos));
        if (chunkProvider.getChunk(x, z) instanceof WorldChunk chunk) {
            int lightSourceLight = LightSources.get(chunk.getWorld()).getLuminance(blockPos);
            if (lightSourceLight > 0) {
                method_51566(blockPos, ChunkLightProvider.class_8531.method_51573(lightSourceLight, false));
            }
        }
    }

    @Inject(method = "method_51530", at = @At("TAIL"))
    private void onMethod_51530(long blockPos, long flags, CallbackInfo info) {
        int x = ChunkSectionPos.getSectionCoord(BlockPos.unpackLongX(blockPos));
        int z = ChunkSectionPos.getSectionCoord(BlockPos.unpackLongZ(blockPos));
        if (chunkProvider.getChunk(x, z) instanceof WorldChunk chunk) {
            int lightLevel = ChunkLightProvider.class_8531.getLightLevel(flags);
            for (Direction direction : DIRECTIONS) {
                int j;
                long m;
                if (!ChunkLightProvider.class_8531.isDirectionBitSet(flags, direction)
                        || !((MutableBlockLightStorage)lightStorage).invokeHasSection(ChunkSectionPos.fromBlockPos(m = BlockPos.offset(blockPos, direction)))
                        || (j = ((MutableBlockLightStorage)lightStorage).invokeGet(m)) == 0) continue;
                if (j <= lightLevel - 1) {
                    int lightSourceLight = LightSources.get(chunk.getWorld()).getLuminance(blockPos);
                    if (lightSourceLight <= 0) continue;
                    method_51566(m, ChunkLightProvider.class_8531.method_51573(lightSourceLight, false));
                    continue;
                }
                method_51566(m, ChunkLightProvider.class_8531.method_51579(j, false, direction.getOpposite()));
            }
        }
    }
}

@Mixin(LightStorage.class)
interface MutableBlockLightStorage {
    @Invoker("hasSection")
    boolean invokeHasSection(long sectionPos);
    @Invoker("get")
    int invokeGet(long blockPos);
    @Invoker("set")
    void invokeSet(long blockPos, int light);
}
