package com.minelittlepony.unicopia.client.render.spell;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.DarkVortexSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.RenderUtil;
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

public class DarkVortexSpellRenderer implements SpellRenderer<DarkVortexSpell> {

    private static float cameraDistortion;

    public static float getCameraDistortion() {
        cameraDistortion *= 0.9F;
        cameraDistortion = MathHelper.clamp(cameraDistortion, 0, 80);
        System.out.println(cameraDistortion);
        return cameraDistortion;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, DarkVortexSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        matrices.push();
        double height = caster.asEntity().getEyeY() - caster.getOriginVector().y;
        matrices.translate(0, height + 2, 0);

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();

        float radius = (float)spell.getEventHorizonRadius();
        float absDistance = (float)cameraEntity.getEyePos().distanceTo(caster.getOriginVector().add(0, 2, 0));

        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getSolid()), light, 1, Math.min(radius, absDistance / 2F), 0, 0, 0, 1);

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

        matrices.push();
        for (int i = 0; i < 10; i++) {
            matrices.scale(0.96F, 1, 1);
            float brightness = i / 10F;
            SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getMagicNoColor()), light, 1, radius * (1 + (0.25F * i)), brightness, brightness, brightness, 0.2F);
        }
        matrices.pop();

        SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayers.getEndPortal()), light, 1, radius * 0.5F, 1, 0.5F, 0, 1);

        matrices.pop();

        if (radius > 1 && absDistance > radius) {
            radius *= 1.1F;

            matrices.scale(radius, radius, radius);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(animationProgress));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(animationProgress * 168));


            RenderUtil.Vertex[] CORNERS = new RenderUtil.Vertex[]{
                    new RenderUtil.Vertex(new Vector3f(-1, -1, 0), 0, 0),
                    new RenderUtil.Vertex(new Vector3f(-1,  1, 0), 1, 0),
                    new RenderUtil.Vertex(new Vector3f( 1,  1, 0), 1, 1),
                    new RenderUtil.Vertex(new Vector3f( 1, -1, 0), 0, 1)
            };

            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("textures/misc/forcefield.png")));

            for (var corner : CORNERS) {
                Vector4f pos = corner.position(matrices);
                buffer.vertex(pos.x, pos.y, pos.z, 1, 1, 1, 1, corner.u(), corner.v(), 0, light, 1, 1, 1);
            }
        }
        matrices.pop();
    }
}
