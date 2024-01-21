package com.minelittlepony.unicopia.client.render.spell;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.PlaceableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PlacedSpellRenderer implements SpellRenderer<PlaceableSpell> {
    private static final Identifier[] TEXTURES = new Identifier[] {
            Unicopia.id("textures/particles/runes_0.png"),
            Unicopia.id("textures/particles/runes_1.png"),
            Unicopia.id("textures/particles/runes_2.png"),
            Unicopia.id("textures/particles/runes_3.png"),
            Unicopia.id("textures/particles/runes_4.png"),
            Unicopia.id("textures/particles/runes_5.png")
    };
    private static final RenderUtil.Vertex[] CORNERS = new RenderUtil.Vertex[]{
            new RenderUtil.Vertex(new Vector3f(-1, -1, 0), 0, 0),
            new RenderUtil.Vertex(new Vector3f(-1,  1, 0), 1, 0),
            new RenderUtil.Vertex(new Vector3f( 1,  1, 0), 1, 1),
            new RenderUtil.Vertex(new Vector3f( 1, -1, 0), 0, 1)
    };

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, PlaceableSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

        if (!(caster.asEntity() instanceof CastSpellEntity)) {
            return;
        }

        for (Spell delegate : spell.getDelegates()) {

            matrices.push();
            matrices.translate(0, 0.001, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spell.pitch));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90 - spell.yaw));
            float scale = (spell.getAge(tickDelta) / 25F) * 3;
            matrices.scale(scale, scale, scale);

            float alpha = scale;

            float angle = (animationProgress / 9F) % 360;

            int color = delegate.getType().getColor();

            float red = Color.r(color);
            float green = Color.g(color);
            float blue = Color.b(color);

            for (int i = 0; i < TEXTURES.length; i++) {
                VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(TEXTURES[i]));

                for (int dim = 0; dim < 3; dim++) {
                    float ringSpeed = (i % 2 == 0 ? i : -1) * i;

                    matrices.push();
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle * ringSpeed));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * ringSpeed * dim));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle * ringSpeed * dim));
                    renderQuad(buffer, matrices, red, green, blue, alpha / ((float)(dim * 3) + 1), light);
                    matrices.pop();
                }
            }

            matrices.pop();

            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertices, delegate, caster, light, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
        }
    }

    protected final void renderQuad(VertexConsumer buffer, MatrixStack matrices, float red, float green, float blue, float alpha, int light) {
        for (var corner : CORNERS) {
            Vector4f pos = corner.position(matrices);
            buffer.vertex(pos.x, pos.y, pos.z, red, green, blue, alpha, corner.u(), corner.v(), 0, light, 1, 1, 1);
        }
    }
}
