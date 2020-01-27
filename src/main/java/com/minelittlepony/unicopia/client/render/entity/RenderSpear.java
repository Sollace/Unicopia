package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.SpearEntity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.Identifier;

public class RenderSpear extends RenderArrow<SpearEntity> {
    public static final Identifier TEXTURE = new Identifier(Unicopia.MODID, "textures/entity/projectiles/spear.png");

    public RenderSpear(RenderManager manager) {
        super(manager);
    }

    @Override
    protected Identifier getEntityTexture(SpearEntity entity) {
        return TEXTURE;
    }
}
