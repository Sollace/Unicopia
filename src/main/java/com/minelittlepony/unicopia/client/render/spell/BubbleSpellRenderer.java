package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.BubbleSpell;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;

public class BubbleSpellRenderer extends SpellRenderer<BubbleSpell> {
    static final int BUBBLE_COLOR = Color.argbToHex(0.9F, 0.9F, 1, 0.25F);
    static final int BUBBLE_SHINE_COLOR = Color.argbToHex(0.9F, 0.9F, 1, 0.3F);

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, BubbleSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        super.render(matrices, vertices, spell, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);

        matrices.push();
        double height = caster.asEntity().getEyeY() - caster.getOriginVector().getY();


        float radius = spell.getRadius(tickDelta) * 1.5F;

        matrices.translate(0, radius * 0.5F + height, 0);

        VertexConsumer buffer = vertices.getBuffer(RenderLayers.getMagicShield());

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(cameraEntity.getYaw(tickDelta) - 25));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraEntity.getPitch(tickDelta)));


        new SphereModel(40, 40, DrawableUtil.PI * 0.25F).render(matrices, buffer, light, 0, radius - 0.1F, BUBBLE_SHINE_COLOR);
        matrices.pop();

        SphereModel.SPHERE.render(matrices, buffer, light, 0, radius, BUBBLE_COLOR);

        matrices.pop();
    }
}
