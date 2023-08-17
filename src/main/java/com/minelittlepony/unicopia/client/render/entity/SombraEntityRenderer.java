package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.SombraEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.util.Identifier;

public class SombraEntityRenderer extends LivingEntityRenderer<SombraEntity, SombraEntityModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/sombra/head.png");

    public SombraEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new SombraEntityModel(SombraEntityModel.getTexturedModelData().createModel()), 0);
    }

    @Override
    public Identifier getTexture(SombraEntity entity) {
        return TEXTURE;
    }

    @Override
    protected boolean hasLabel(SombraEntity targetEntity) {
        return false;
    }
}