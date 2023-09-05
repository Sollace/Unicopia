package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
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
}
