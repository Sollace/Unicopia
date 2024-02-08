package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.DarkVortexSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class DarkVortexSpellRenderer extends SpellRenderer<DarkVortexSpell> {

    private static final Identifier ACCRETION_DISK_TEXTURE = Unicopia.id("textures/spells/dark_vortex/accretion_disk.png");

    private static float cameraDistortion;

    public static float getCameraDistortion() {
        cameraDistortion *= 0.9F;
        cameraDistortion = MathHelper.clamp(cameraDistortion, 0, 80);
        return cameraDistortion;
    }

    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass < 2;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, DarkVortexSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        super.render(matrices, vertices, spell, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();

        float radius = (float)spell.getEventHorizonRadius();
        float absDistance = (float)cameraEntity.getEyePos().distanceTo(caster.getOriginVector().add(0, 2, 0));

        matrices.push();
        matrices.translate(0, 2 + radius, 0);

        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getSolid()), light, 1, Math.min(radius * 0.6F, absDistance * 0.1F), 0, 0, 0, 1);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90 + cameraEntity.getYaw(tickDelta)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-cameraEntity.getPitch(tickDelta)));

        matrices.scale(0.7F, 1, 1);

        float distance = 1F / MathHelper.clamp((absDistance / (radius * 4)), 0.0000001F, 1);
        distance *= distance;
        if (absDistance < radius * 4) {
            cameraDistortion += distance;
        }

        matrices.scale(distance, distance, distance);

        if (absDistance > radius) {
            matrices.push();
            matrices.translate(0, -0.1F, 0);
            for (int i = 0; i < 10; i++) {
                matrices.scale(1, 1, 0.796F);
                float brightness = i / 10F;
                SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getMagicNoColor()), light, 1, radius * (1 + (0.25F * i)) * 0.7F, brightness, brightness, brightness, 0.2F);
            }
            matrices.pop();
        }

        SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getEndPortal()), light, 1, radius * 0.5F, 1, 0.5F, 0, 1);

        if (radius > 0.3F && absDistance > radius) {
            radius *= Math.min(2, 3 + radius);

            matrices.scale(radius, radius, radius);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(animationProgress * 168));

            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(ACCRETION_DISK_TEXTURE));

            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, 1, 1, 1, 1);

            matrices.push();
            matrices.scale(0.5F, 0.5F, 0.5F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(33));

            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, 1, 1, 1, 1);
            matrices.pop();

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45));
            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, 1, 1, 1, 1);
        }
        matrices.pop();
        matrices.pop();
    }

    @Override
    protected void renderCountdown(MatrixStack matrices, TimedSpell spell, float tickDelta) {

    }
}
