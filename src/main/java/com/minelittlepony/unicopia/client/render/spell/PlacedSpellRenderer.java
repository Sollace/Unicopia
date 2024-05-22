package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.PlaceableSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PlacedSpellRenderer extends SpellRenderer<PlaceableSpell> {
    private static final Identifier[] TEXTURES = new Identifier[] {
            Unicopia.id("textures/particles/runes_0.png"),
            Unicopia.id("textures/particles/runes_1.png"),
            Unicopia.id("textures/particles/runes_2.png"),
            Unicopia.id("textures/particles/runes_3.png"),
            Unicopia.id("textures/particles/runes_4.png"),
            Unicopia.id("textures/particles/runes_5.png")
    };

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, PlaceableSpell spell, Caster<?> caster, int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        matrices.push();

        Spell delegate = spell.getDelegate();

        if (delegate != null) {
            renderAmbientEffects(matrices, vertices, spell, delegate, caster, light, animationProgress, tickDelta);

            matrices.push();
            float height = caster.asEntity().getHeight();
            matrices.translate(0, (-spell.pitch / 90F) * height * 0.5F, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spell.yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-spell.pitch));
            SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertices, delegate, caster, light, spell.getScale(tickDelta), limbDistance, tickDelta, animationProgress, headYaw, headPitch);
            matrices.pop();
        }

        matrices.pop();
    }

    protected void renderAmbientEffects(MatrixStack matrices, VertexConsumerProvider vertices, PlaceableSpell spell, Spell delegate, Caster<?> caster, int light, float animationProgress, float tickDelta) {
        matrices.push();
        matrices.translate(0, 0.001, 0);

        float height = caster.asEntity().getHeight();
        matrices.translate(0, (-spell.pitch / 90F) * height * 0.5F, 0);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spell.yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-spell.pitch));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        float scale = spell.getScale(tickDelta) * 3;
        matrices.scale(scale, scale, scale);

        float angle = (animationProgress / 9F) % 360;

        int color = delegate.getTypeAndTraits().type().getColor();

        float red = Color.r(color);
        float green = Color.g(color);
        float blue = Color.b(color);

        SpellRenderer<?> renderer = SpellEffectsRenderDispatcher.INSTANCE.getRenderer(delegate);

        for (int i = 0; i < TEXTURES.length; i++) {
            if (!renderer.shouldRenderEffectPass(i)) {
                continue;
            }
            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(TEXTURES[i]));

            for (int dim = 0; dim < 3; dim++) {
                float ringSpeed = (i % 2 == 0 ? i : -1) * i;

                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle * ringSpeed));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * ringSpeed * dim));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle * ringSpeed * dim));
                PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, red, green, blue, scale / ((float)(dim * 3) + 1));
                matrices.pop();
            }
        }

        matrices.pop();
    }
}
