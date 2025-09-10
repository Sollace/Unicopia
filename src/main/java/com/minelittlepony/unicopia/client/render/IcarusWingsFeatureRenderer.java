package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.util.Identifier;

public class IcarusWingsFeatureRenderer<S extends BipedEntityRenderState> extends WingsFeatureRenderer<S> {
    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted.png");

    public IcarusWingsFeatureRenderer(FeatureRendererContext<S, ? extends BipedEntityModel<S>> context) {
        super(context);
    }

    @Override
    protected boolean canRender(S entity) {
        return !super.canRender(entity) && CasterState.of(entity).pegasusAmulet;
    }

    @Override
    protected Identifier getTexture(S entity) {
        return CasterState.of(entity).inHell ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
    }
}
