package com.minelittlepony.unicopia.mixin.server;

import java.util.stream.LongStream;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.server.world.Ether;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

@Mixin(ChunkDataSender.class)
abstract class MixinChunkDataSender {
    @Shadow
    private @Final LongSet chunks;

    @Inject(method = "add", at = @At("RETURN"))
    private void onAdd(WorldChunk chunk, CallbackInfo info) {
        var etherChunk = Ether.get(chunk.getWorld()).getChunk(chunk.getPos());
        if (etherChunk != null) {
            etherChunk.getStates().stream().flatMapToLong(state -> {
                if (state.getSpell() instanceof PortalSpell portal && portal.getDestinationReference().isSet()) {
                    return portal.getDestinationReference()
                        .getTarget()
                        .stream()
                        .mapToLong(target -> ChunkPos.toLong(BlockPos.ofFloored(target.pos())));
                }
                return LongStream.empty();
            }).distinct().forEach(chunks::add);
        }
    }
}
