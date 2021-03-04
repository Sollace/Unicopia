package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.CastSpellEntity;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public class CastSpellEntityRenderer extends EntityRenderer<CastSpellEntity> {

    public CastSpellEntityRenderer(EntityRenderDispatcher dispatcher, EntityRendererRegistry.Context context) {
        super(dispatcher);
    }

    @Override
    public Identifier getTexture(CastSpellEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
