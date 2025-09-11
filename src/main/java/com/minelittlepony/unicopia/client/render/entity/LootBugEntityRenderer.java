package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.entity.SilverfishEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.mob.SilverfishEntity;

public class LootBugEntityRenderer extends SilverfishEntityRenderer {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/loot_bug.png");

    public LootBugEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(LivingEntityRenderState entity) {
        return TEXTURE;
    }

    @Override
    public void updateRenderState(SilverfishEntity entity, LivingEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.baseScale *= 2;
    }
}
