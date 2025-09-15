package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

class SpellEffectGear implements Gear {
    private CasterState caster;

    @Override
    public boolean canRender(PonyModel<?> model, EntityRenderState entity) {
        return CasterState.of(entity).present;
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.LEGS;
    }

    @Override
    public <S extends EntityRenderState> Identifier getTexture(S entity, Context<S, ?> context) {
        return BraceletFeatureRenderer.TEXTURE;
    }

    @Override
    public <S extends EntityRenderState & PonyModel.AttributedHolder> void transform(S state, PonyModel<S> model, MatrixStack matrices) {
    }

    @Override
    public <S extends BipedEntityRenderState & PonyModel.AttributedHolder> void pose(PonyModel<S> model, S state, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        caster = CasterState.of(state);
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        SpellEffectsRenderDispatcher.INSTANCE.render(
            stack,
            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
            light, caster
        );
    }
}
