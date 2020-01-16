package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.EntitySpear;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.Identifier;

public class RenderSpear extends RenderArrow<EntitySpear> {
    public static final Identifier TEXTURE = new Identifier(Unicopia.MODID, "textures/entity/projectiles/spear.png");

    public RenderSpear(RenderManager manager) {
        super(manager);
    }

    @Override
    protected Identifier getEntityTexture(EntitySpear entity) {
        return TEXTURE;
    }
}
