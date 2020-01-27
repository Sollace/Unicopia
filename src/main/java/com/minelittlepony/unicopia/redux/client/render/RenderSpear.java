package com.minelittlepony.unicopia.redux.client.render;

import com.minelittlepony.unicopia.core.UnicopiaCore;
import com.minelittlepony.unicopia.redux.entity.SpearEntity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.Identifier;

public class RenderSpear extends RenderArrow<SpearEntity> {
    public static final Identifier TEXTURE = new Identifier(UnicopiaCore.MODID, "textures/entity/projectiles/spear.png");

    public RenderSpear(RenderManager manager) {
        super(manager);
    }

    @Override
    protected Identifier getEntityTexture(SpearEntity entity) {
        return TEXTURE;
    }
}
