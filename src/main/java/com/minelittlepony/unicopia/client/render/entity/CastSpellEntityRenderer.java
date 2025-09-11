package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;

public class CastSpellEntityRenderer extends EntityRenderer<CastSpellEntity, CastSpellEntityRenderer.State> {
    private static final Identifier[] TEXTURES = new Identifier[] {
            Unicopia.id("textures/particles/runes_0.png"),
            Unicopia.id("textures/particles/runes_1.png"),
            Unicopia.id("textures/particles/runes_2.png"),
            Unicopia.id("textures/particles/runes_3.png"),
            Unicopia.id("textures/particles/runes_4.png"),
            Unicopia.id("textures/particles/runes_5.png")
    };

    public CastSpellEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }


    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(CastSpellEntity entity, State state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.angle = (state.age / 9F) % 360;
        state.height = entity.getHeight();
        state.scale = entity.getScale(tickDelta);
        state.pitch = entity.getPitch(tickDelta);
        state.yaw = entity.getYaw(tickDelta);
        state.yOffset = (-state.pitch / 90F) * state.height * 0.5F;
        state.state.update(entity, tickDelta);
    }

    @Override
    protected boolean canBeCulled(CastSpellEntity entity) {
        return false;
    }

    @Override
    public void render(State state, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.cameraEntity instanceof CastSpellEntity) {
            return;
        }
        matrices.push();
        matrices.translate(0, 0.001 + state.yOffset, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-state.pitch));

        SpellRenderState spellState = state.state.spells.getFirst();
        if (spellState != null) {
            renderAmbientEffects(matrices, vertices, state, spellState, light);
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertices, light, state.state, spellState);
        }
        matrices.pop();
    }

    protected void renderAmbientEffects(MatrixStack matrices, VertexConsumerProvider vertices, State state, SpellRenderState spell, int light) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        float scale = state.scale * 3;
        matrices.scale(scale, scale, scale);

        @Nullable
        SpellRenderer<?, ?> renderer = spell == null ? null : SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell);

        for (int i = 0; i < TEXTURES.length; i++) {
            if (renderer != null && !renderer.shouldRenderEffectPass(i)) {
                continue;
            }
            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(TEXTURES[i]));

            for (int dim = 0; dim < 3; dim++) {
                float ringSpeed = (i % 2 == 0 ? i : -1) * i;

                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(state.angle * ringSpeed));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(state.angle * ringSpeed * dim));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(state.angle * ringSpeed * dim));
                PlaneModel.INSTANCE.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV, 1, ColorHelper.withAlpha(spell.type.type().getColor(), (int)(255 * (scale / ((float)(dim * 3) + 1)))));
                matrices.pop();
            }
        }

        matrices.pop();
    }

    public static class State extends EntityRenderState {
        public float scale;
        public float pitch;
        public float yaw;

        public float yOffset;

        public float angle;

        public CasterState state = new CasterState(this);
    }
}
