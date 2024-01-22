package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.ShieldSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.util.ColorHelper;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class ShieldSpellRenderer extends SpellRenderer<ShieldSpell> {
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, ShieldSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        super.render(matrices, vertices, spell, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);

        matrices.push();
        double height = caster.asEntity().getEyeY() - caster.getOriginVector().y;
        matrices.translate(0, height, 0);

        int color = ColorHelper.lerp(caster.getCorruption().getScaled(1) * (tickDelta / (1 + caster.asWorld().random.nextFloat())), spell.getType().getColor(), 0xFF000);
        float[] colors = ColorHelper.changeSaturation(Color.r(color), Color.g(color), Color.b(color), 4);
        float radius = 0.35F + spell.getRadius(tickDelta) + MathHelper.sin(animationProgress / 30F) * 0.01F;

        VertexConsumer buffer = vertices.getBuffer(RenderLayers.getMagicShield());

        float thickness = 0.02F * MathHelper.sin(animationProgress / 30F);
        float alpha = 1 - Math.abs(MathHelper.sin(animationProgress / 20F)) * 0.2F;
        SphereModel.SPHERE.render(matrices, buffer, light, 1, radius + thickness, colors[0], colors[1], colors[2], alpha * 0.08F);
        SphereModel.SPHERE.render(matrices, buffer, light, 1, radius - thickness, colors[0], colors[1], colors[2], alpha * 0.05F);
        SphereModel.SPHERE.render(matrices, buffer, light, 1, radius + thickness * 2, colors[0], colors[1], colors[2], alpha * 0.05F);

        matrices.pop();
    }
}
