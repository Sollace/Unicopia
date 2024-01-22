package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class PortalSpellRenderer extends SpellRenderer<PortalSpell> {

    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass == 0;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, PortalSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        super.render(matrices, vertices, spell, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);

        if (!spell.isLinked()) {
            return;
        }

        int color = spell.getType().getColor();

        float red = Color.r(color);
        float green = Color.g(color);
        float blue = Color.b(color);

        VertexConsumer buffer = vertices.getBuffer(RenderLayers.getEndGateway());

        double thickness = 0.1;

        matrices.push();
        matrices.translate(0, thickness, 0);
        SphereModel.DISK.render(matrices, buffer, light, 0, 2.5F, red, green, blue, 1);
        matrices.pop();

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
        matrices.translate(0, thickness, 0);
        SphereModel.DISK.render(matrices, buffer, light, 0, 2.5F, red, green, blue, 1);

        matrices.pop();
    }
}
