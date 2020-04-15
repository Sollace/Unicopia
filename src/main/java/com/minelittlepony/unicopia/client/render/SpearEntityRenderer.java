package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.SpearEntity;

import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;

public class SpearEntityRenderer extends ProjectileEntityRenderer<SpearEntity> {
    public static final Identifier TEXTURE = new Identifier(Unicopia.MODID, "textures/entity/projectiles/spear.png");

    public SpearEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager);
    }

    @Override
    protected Identifier getTexture(SpearEntity entity) {
        return TEXTURE;
    }
}
