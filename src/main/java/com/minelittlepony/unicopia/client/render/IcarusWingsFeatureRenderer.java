package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Identifier;

public class IcarusWingsFeatureRenderer<S extends BipedEntityRenderState> extends WingsFeatureRenderer<S> {
    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted.png");

    public IcarusWingsFeatureRenderer(FeatureRendererContext<S, ? extends BipedEntityModel<S>> context) {
        super(context);
    }

    @Override
    protected boolean canRender(S state) {
        return !super.canRender(state) && CasterState.of(state).pegasusAmulet;
    }

    @Override
    protected VertexConsumer getBuffer(VertexConsumerProvider vertices, Identifier texture) {
        return ItemRenderer.getArmorGlintConsumer(vertices, RenderLayer.getEntityTranslucent(texture), true);
    }

    @Override
    protected Identifier getTexture(S state) {
        return CasterState.of(state).inHell ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
    }

    @Override
    @Nullable
    protected Identifier getOverlayTexture(S state) {
        return null;
    }
}
