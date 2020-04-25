package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.SpearEntity;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.util.Identifier;

public class SpearEntityRenderer extends ProjectileEntityRenderer<SpearEntity> {
    public static final Identifier TEXTURE = new Identifier("unicopia", "textures/entity/projectiles/spear.png");

    public SpearEntityRenderer(EntityRenderDispatcher manager, EntityRendererRegistry.Context context) {
        super(manager);
    }

    @Override
    public Identifier getTexture(SpearEntity entity) {
        return TEXTURE;
    }
}
