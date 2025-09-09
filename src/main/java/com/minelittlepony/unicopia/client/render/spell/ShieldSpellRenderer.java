package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.ShieldSpell;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.util.ColorHelper;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import static net.minecraft.util.math.ColorHelper.*;

public class ShieldSpellRenderer extends SpellRenderer<ShieldSpell, ShieldSpellRenderer.State> {
    private final SphereModel model = new SphereModel(40, 40, DrawableUtil.PI);

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(ShieldSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.height = caster.asEntity().getEyeY() - caster.getOriginVector().y;
        int typeColor = state.type.type().getColor();
        int ponyColor = MineLPDelegate.getInstance().getMagicColor(caster.getOriginatingCaster().asEntity());

        state.color = ColorHelper.saturate(lerp(
                caster.getCorruption().getScaled(1) * (tickDelta / (1 + caster.asWorld().random.nextFloat())),
                ponyColor == 0 ? typeColor : lerp(0.6F, ponyColor, typeColor),
                0xFF000
        ), 2);
        state.radius = 0.35F + spell.getRadius(tickDelta) + MathHelper.sin((caster.asEntity().age + tickDelta) / 30F) * 0.01F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, State spell, CasterState caster, int light, float limbAngle, float limbDistance) {
        super.render(matrices, vertices, spell, caster, light, limbAngle, limbDistance);

        matrices.push();
        matrices.translate(0, spell.height, 0);

        VertexConsumer buffer = vertices.getBuffer(RenderLayers.getMagicShield());

        float thickness = 0.02F * MathHelper.sin(caster.entityState.age / 30F);
        float alpha = (1 - Math.abs(MathHelper.sin(caster.entityState.age / 20F)) * 0.1F) * MathHelper.clamp(spell.radius - 1, 0, 1);

        if (caster.isCamera) {
            matrices.translate(0, -1.75F, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(client.cameraEntity.getPitch(client.getRenderTickCounter().getTickDelta(false))));
            model.render(matrices, buffer, light, 1, spell.radius, withAlpha((int)((alpha * 0.2F) * 255), spell.color));
        } else {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            matrices.scale(1, spell.radius == 0 ? 1 : MathHelper.clamp(2.6F / spell.radius, 0.7F, 1.8F), 1);
            SphereModel.SPHERE.render(matrices, buffer, light, 1, spell.radius + thickness, withAlpha((int)((alpha * 0.08F) * 255), spell.color));
            SphereModel.SPHERE.render(matrices, buffer, light, 1, spell.radius - thickness, withAlpha((int)((alpha * 0.05F) * 255), spell.color));
            SphereModel.SPHERE.render(matrices, buffer, light, 1, spell.radius + thickness * 2, withAlpha((int)((alpha * 0.05F) * 255), spell.color));
        }

        matrices.pop();
    }

    public static class State extends SpellRenderState {
        public double height;
        public int color;
        public float radius;
    }
}
