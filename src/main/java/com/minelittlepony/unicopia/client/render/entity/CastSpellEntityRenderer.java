package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper.Argb;
import net.minecraft.util.math.RotationAxis;

public class CastSpellEntityRenderer extends EntityRenderer<CastSpellEntity> {
    private static final Identifier[] TEXTURES = new Identifier[] {
            Unicopia.id("textures/particles/runes_0.png"),
            Unicopia.id("textures/particles/runes_1.png"),
            Unicopia.id("textures/particles/runes_2.png"),
            Unicopia.id("textures/particles/runes_3.png"),
            Unicopia.id("textures/particles/runes_4.png"),
            Unicopia.id("textures/particles/runes_5.png")
    };

    public CastSpellEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(CastSpellEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(CastSpellEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        matrices.push();
        matrices.translate(0, 0.001, 0);
        final float height = entity.getHeight();
        final float pitch = entity.getPitch(tickDelta);
        matrices.translate(0, (-pitch / 90F) * height * 0.5F, 0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));

        float animationProgress = getAnimationProgress(entity, tickDelta);
        renderAmbientEffects(matrices, vertices, entity, entity.getSpellSlot().get().orElse(null), light, animationProgress, tickDelta);
        SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertices, light, entity, entity.getScale(tickDelta), 0, tickDelta, animationProgress, yaw, pitch);

        matrices.pop();
    }

    protected float getAnimationProgress(CastSpellEntity entity, float tickDelta) {
        return entity.age + tickDelta;
    }

    protected void renderAmbientEffects(MatrixStack matrices, VertexConsumerProvider vertices, CastSpellEntity entity, @Nullable Spell spell, int light, float animationProgress, float tickDelta) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));

        float scale = entity.getScale(tickDelta) * 3;
        matrices.scale(scale, scale, scale);

        float angle = (animationProgress / 9F) % 360;

        int color = spell == null ? 0 : spell.getTypeAndTraits().type().getColor();

        @Nullable
        SpellRenderer<?> renderer = spell == null ? null : SpellEffectsRenderDispatcher.INSTANCE.getRenderer(spell);

        for (int i = 0; i < TEXTURES.length; i++) {
            if (renderer != null && !renderer.shouldRenderEffectPass(i)) {
                continue;
            }
            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(TEXTURES[i]));

            for (int dim = 0; dim < 3; dim++) {
                float ringSpeed = (i % 2 == 0 ? i : -1) * i;

                matrices.push();
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle * ringSpeed));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle * ringSpeed * dim));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle * ringSpeed * dim));
                PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, Argb.withAlpha(color, (int)(255 * (scale / ((float)(dim * 3) + 1)))));
                matrices.pop();
            }
        }

        matrices.pop();
    }
}
