package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.mob.SombraEntity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.util.Identifier;

public class SombraEntityRenderer extends LivingEntityRenderer<SombraEntity, SombraEntityRenderer.State, SombraEntityModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/sombra/head.png");

    public SombraEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SombraEntityModel(SombraEntityModel.getTexturedModelData().createModel()), 0);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(SombraEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.nameLabelPos = null;
        state.jawsOpenAmount = entity.getBiteAmount(tickDelta);
        state.scale = entity.getScaleFactor(tickDelta) * 1.7F;
    }

    @Override
    public Identifier getTexture(State entity) {
        return TEXTURE;
    }

    public static class State extends LivingEntityRenderState {
        public float jawsOpenAmount;
        public float scale;
    }
}