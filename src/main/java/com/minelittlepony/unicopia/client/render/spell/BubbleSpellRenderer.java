package com.minelittlepony.unicopia.client.render.spell;

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

public class BubbleSpellRenderer implements SpellRenderer<BubbleSpell> {
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, BubbleSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        matrices.push();
        double height = caster.asEntity().getEyeY() - caster.getOriginVector().y;
        matrices.translate(0, height * 0.5F, 0);

        float radius = spell.getRadius(tickDelta) * 0.7F;

        VertexConsumer buffer = vertices.getBuffer(RenderLayers.getMagicNoColor());

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-45));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(45 + cameraEntity.getYaw(tickDelta)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-cameraEntity.getPitch(tickDelta)));

        new SphereModel(40, 40, DrawableUtil.PI * 0.25F).render(matrices, buffer, light, 0, radius - 0.1F, 0.9F, 0.9F, 1, 0.3F);
        matrices.pop();

        SphereModel.SPHERE.render(matrices, buffer, light, 0, radius, 0.9F, 0.9F, 1, 0.25F);

        matrices.pop();
    }
}
