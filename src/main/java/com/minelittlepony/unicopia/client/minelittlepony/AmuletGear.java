package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.AmuletFeatureRenderer.AmuletModel;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

class AmuletGear extends AmuletModel implements Gear {

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    public AmuletGear() {
        super(AmuletModel.getData(new Dilation(0.3F)).createModel());
    }

    @Override
    public boolean canRender(PonyModel<?> model, EntityRenderState entity) {
        return CasterState.of(entity).amulet.stack().isEmpty();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.BODY;
    }

    @Override
    public <S extends EntityRenderState> Identifier getTexture(S entity, Context<S, ?> context) {
        return textures.computeIfAbsent(Registries.ITEM.getId(CasterState.of(entity).amulet.stack().getItem()), id -> id.withPath(p  -> "textures/models/armor/" + p + ".png"));
    }

    @Override
    public <S extends EntityRenderState & PonyModel.AttributedHolder> void transform(S state, PonyModel<S> model, MatrixStack matrices) {
        BodyPart part = getGearLocation();
        model.transform(state, part, matrices);
        matrices.translate(0, 0.25, 0);
    }

    @Override
    public <S extends BipedEntityRenderState & PonyModel.AttributedHolder> void pose(PonyModel<S> model, S state, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        if (model instanceof BipedEntityModel<?> biped) {
            setAngles(biped);
        }
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        render(stack, consumer, light, overlay, color);
    }
}
