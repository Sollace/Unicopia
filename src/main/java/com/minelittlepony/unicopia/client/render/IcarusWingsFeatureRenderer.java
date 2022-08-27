package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class IcarusWingsFeatureRenderer<E extends LivingEntity> extends WingsFeatureRenderer<E> {
    private static final Identifier ICARUS_WINGS = Unicopia.id("textures/models/wings/icarus.png");
    private static final Identifier ICARUS_WINGS_CORRUPTED = Unicopia.id("textures/models/wings/icarus_corrupted.png");

    public IcarusWingsFeatureRenderer(FeatureRendererContext<E, ? extends BipedEntityModel<E>> context) {
        super(context);
    }

    @Override
    protected boolean canRender(E entity) {
        return !super.canRender(entity) && UItems.PEGASUS_AMULET.isApplicable(entity);
    }

    @Override
    protected Identifier getTexture(E entity) {
        return entity.world.getDimension().ultrawarm() ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
    }
}
