package com.minelittlepony.unicopia.client.render;

import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.FruitBearingBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public final class LeavesAdditionsModel implements BlockStateModel {
    public static void bootstrap() {
        ModelLoadingPlugin.register(ctx -> {
            ctx.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
                if (!(context.state().getBlock() instanceof FruitBearingBlock) || model instanceof LeavesAdditionsModel) {
                    return model;
                }
                return new LeavesAdditionsModel(model);
            });
        });
    }

    private final BlockStateModel wrapped;

    private LeavesAdditionsModel(BlockStateModel model) {
        this.wrapped = model;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
        MaterialFinder finder = Renderer.get().materialFinder();
        emitter.pushTransform(quad -> {
            quad.material(finder.copyFrom(quad.material()).blendMode(BlendMode.CUTOUT).find());
            return true;
        });
        BlockStateModel.super.emitQuads(emitter, blockView, pos, state, random, cullTest);
        emitter.popTransform();
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {
        wrapped.addParts(random, parts);
    }

    @Override
    public Sprite particleSprite() {
        return wrapped.particleSprite();
    }
}
