package com.minelittlepony.unicopia.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.EntitySpear;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderSpear extends RenderArrow<EntitySpear> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Unicopia.MODID, "textures/entity/projectiles/spear.png");

    public RenderSpear(RenderManager manager) {
        super(manager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySpear entity) {
        return TEXTURE;
    }
}
