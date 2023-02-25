package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.IModel;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.unicopia.client.render.GlassesFeatureRenderer.GlassesModel;
import com.minelittlepony.unicopia.item.GlassesItem;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

class GlassesGear extends GlassesModel implements IGear {

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    public GlassesGear() {
        super(GlassesModel.getData(new Dilation(0.3F), -6, -6).createModel());
    }

    @Override
    public boolean canRender(IModel model, Entity entity) {
        return entity instanceof LivingEntity living && !GlassesItem.getForEntity(living).isEmpty();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.HEAD;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return textures.computeIfAbsent(Registry.ITEM.getId(GlassesItem.getForEntity((LivingEntity)entity).getItem()), id -> new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + ".png"));
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, UUID interpolatorId) {
        render(stack, consumer, light, overlay, red, green, blue, 1);
    }
}
