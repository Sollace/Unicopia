package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.CastSpellEntity;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public class CastSpellEntityRenderer extends EntityRenderer<CastSpellEntity> {

    public CastSpellEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(CastSpellEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
