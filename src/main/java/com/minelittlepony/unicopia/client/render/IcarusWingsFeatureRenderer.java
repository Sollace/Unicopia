package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.AmuletSelectors;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
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
        return !super.canRender(entity) && AmuletSelectors.PEGASUS_AMULET.test(entity);
    }

    @Override
    protected VertexConsumer getBuffer(VertexConsumerProvider vertices, Identifier texture) {
        return ItemRenderer.getArmorGlintConsumer(vertices, RenderLayer.getEntityTranslucent(texture), true);
    }

    @Override
    protected Identifier getTexture(E entity) {
        return entity.getWorld().getDimension().ultrawarm() ? ICARUS_WINGS_CORRUPTED : ICARUS_WINGS;
    }

    @Override
    @Nullable
    protected Identifier getOverlayTexture(E entity) {
        return null;
    }
}
