package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class CastSpellEntityRenderer extends EntityRenderer<CastSpellEntity> {
    public CastSpellEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(CastSpellEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    @Override
    public void render(CastSpellEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertexConsumers, light, entity, 0, 0, tickDelta, getAnimationProgress(entity, tickDelta), yaw, 0);
    }

    protected float getAnimationProgress(CastSpellEntity entity, float tickDelta) {
        return entity.age + tickDelta;
    }
}
