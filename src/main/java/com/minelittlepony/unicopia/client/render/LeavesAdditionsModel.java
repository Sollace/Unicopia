package com.minelittlepony.unicopia.client.render;

import java.util.function.Supplier;

import com.minelittlepony.unicopia.block.FruitBearingBlock;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public final class LeavesAdditionsModel extends ForwardingBakedModel {
    public static void bootstrap() {
        ModelLoadingPlugin.register(ctx -> {
            ctx.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
                Identifier id = context.resourceId();
                if (!id.getPath().endsWith("_flowering") || !(Registries.BLOCK.get(id.withPath(p -> p.replace("block/", "").replace("_flowering", ""))) instanceof FruitBearingBlock)) {
                    return model;
                }
                return model == null ? null : new LeavesAdditionsModel(model);
            });
        });
    }

    private LeavesAdditionsModel(BakedModel model) {
        this.wrapped = model;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        MaterialFinder finder = RendererAccess.INSTANCE.getRenderer().materialFinder();
        context.pushTransform(quad -> {
            quad.material(finder.copyFrom(quad.material()).blendMode(BlendMode.CUTOUT).find());
            return true;
        });
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }
}
