package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.BubbleSpell;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.RotationAxis;

public class BubbleSpellRenderer extends SpellRenderer<BubbleSpell, BubbleSpellRenderer.State> {
    static final int BUBBLE_COLOR = ColorHelper.fromFloats(0.9F, 0.9F, 1, 0.25F);
    static final int BUBBLE_SHINE_COLOR = ColorHelper.fromFloats(0.9F, 0.9F, 1, 0.3F);

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(BubbleSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.height = caster.asEntity().getEyeY() - caster.getOriginVector().getY();
        state.radius = spell.getRadius(tickDelta) * 1.5F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, State state, CasterState caster, int light) {
        super.render(matrices, vertices, state, caster, light);

        matrices.push();

        matrices.translate(0, state.radius * 0.5F + state.height, 0);

        VertexConsumer buffer = vertices.getBuffer(RenderLayers.getMagicShield());

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(cameraEntity.getYaw(tickDelta) - 25));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraEntity.getPitch(tickDelta)));

        new SphereModel(40, 40, DrawableUtil.PI * 0.25F).render(matrices, buffer, light, 0, state.radius - 0.1F, BUBBLE_SHINE_COLOR);
        matrices.pop();

        SphereModel.SPHERE.render(matrices, buffer, light, 0, state.radius, BUBBLE_COLOR);

        matrices.pop();
    }

    static class State extends SpellRenderState {
        public double height;
        public float radius;
    }
}
