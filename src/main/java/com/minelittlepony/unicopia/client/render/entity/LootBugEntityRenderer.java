package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.entity.SilverfishEntityRenderer;

public class LootBugEntityRenderer extends SilverfishEntityRenderer {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/loot_bug.png");

    public LootBugEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(SilverfishEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(SilverfishEntity entity, MatrixStack matrices, float tickDelta) {
        float scale = 2;
        matrices.scale(scale, scale, scale);
    }
}
