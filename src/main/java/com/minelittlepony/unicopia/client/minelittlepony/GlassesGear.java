package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.GlassesFeatureRenderer.GlassesModel;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

class GlassesGear extends GlassesModel implements Gear {

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    public GlassesGear() {
        super(GlassesModel.getData(new Dilation(0.3F), -6, -6).createModel());
    }

    @Override
    public boolean canRender(PonyModel<?> model, EntityRenderState entity) {
        return !CasterState.of(entity).eyewear.stack().isEmpty();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.HEAD;
    }

    @Override
    public <S extends EntityRenderState> Identifier getTexture(S entity, Context<S, ?> context) {
        return textures.computeIfAbsent(CasterState.of(entity).eyewear.stack().getRegistryEntry().getKey().get().getValue(), id -> id.withPath(p -> "textures/models/armor/" + p + ".png"));
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        render(stack, consumer, light, overlay, color);
    }
}
