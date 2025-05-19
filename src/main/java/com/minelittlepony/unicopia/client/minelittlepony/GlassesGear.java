package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.GlassesFeatureRenderer.GlassesModel;
import com.minelittlepony.unicopia.item.GlassesItem;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

class GlassesGear extends GlassesModel implements Gear {

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    public GlassesGear() {
        super(GlassesModel.getData(new Dilation(0.3F), -6, -6).createModel());
    }

    @Override
    public boolean canRender(PonyModel<?> model, Entity entity) {
        return entity instanceof LivingEntity living && !GlassesItem.getForEntity(living).stack().isEmpty();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.HEAD;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return textures.computeIfAbsent(Registries.ITEM.getId(GlassesItem.getForEntity((LivingEntity)entity).stack().getItem()), id -> id.withPath(p -> "textures/models/armor/" + p + ".png"));
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        render(stack, consumer, light, overlay, color);
    }
}
