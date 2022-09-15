package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.AirBalloonEntity;

import net.minecraft.client.render.entity.*;
import net.minecraft.util.Identifier;

public class AirBalloonEntityRenderer extends MobEntityRenderer<AirBalloonEntity, AirBalloonEntityModel> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/air_balloon.png");

    public AirBalloonEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new AirBalloonEntityModel(AirBalloonEntityModel.getTexturedModelData().createModel()), 0);
    }

    @Override
    public Identifier getTexture(AirBalloonEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getLyingAngle(AirBalloonEntity entity) {
        return 0;
    }
}