package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.DarkVortexSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
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
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        float radius = (float)spell.getEventHorizonRadius();

        float absDistance = (float)camera.getPos().distanceTo(spell.getOrigin(caster));

        matrices.push();
        matrices.translate(0, spell.getYOffset(), 0);

        float visualRadius = Math.min(radius * 0.8F, absDistance - 1F);

        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getSolid()), light, 1, visualRadius, 0, 0, 0, 1);
        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getMagicColored()), light, 1, visualRadius + 0.05F, 0, 0, 0, 0.9F);
        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getMagicColored()), light, 1, visualRadius + 0.1F, 0, 0, 0, 0.9F);
        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getMagicColored()), light, 1, visualRadius + 0.15F, 0, 0, 0, 0.9F);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90));
        matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90 + camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-camera.getPitch()));

        matrices.scale(0.7F, 1, 1);

        float distance = 1F / MathHelper.clamp(absDistance / (radius + 7), 0.0000001F, 1);
        distance *= distance;
        if (absDistance < radius * 4) {
            cameraDistortion += distance;
        }

        SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getEndPortal()), light, 1, radius * 0.5F, 0, 0, 0, 0);

        matrices.push();
        matrices.scale(distance, distance, distance);

        if (absDistance > radius) {
            matrices.push();
            matrices.translate(0, -0.1F, 0);
            for (int i = 0; i < 10; i++) {
                float brightness = i / 10F;
                SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getMagicNoColor()), light, 1, radius * (1 + (0.25F * i)) * 0.7F, brightness, brightness, brightness, 0.2F);
            }
            matrices.pop();
        }
        matrices.pop();



        if (radius > 0.3F && absDistance > radius) {
            radius *= Math.min(2, 3 + radius);

            float processionSpeed = animationProgress * 0.02F;
            float maxProcessionAngle = 15;

            float range = (float)spell.getDrawDropOffRange() / 8F;

            matrices.scale(range, range, range);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90 + MathHelper.cos(processionSpeed) * maxProcessionAngle));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.sin(processionSpeed) * maxProcessionAngle));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(animationProgress * 18));

            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(ACCRETION_DISK_TEXTURE));

            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, 1, 1, 1, 1);



            matrices.scale(0.9F, 0.9F, 0.9F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(33));

            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, 1, 1, 1, 1);

            matrices.scale(0.9F, 0.9F, 0.9F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(33));
            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, 1, 1, 1, 1);
        }
        matrices.pop();
        matrices.pop();
    }

    @Override
    protected void renderCountdown(MatrixStack matrices, TimedSpell spell, float tickDelta) {

    }
}
