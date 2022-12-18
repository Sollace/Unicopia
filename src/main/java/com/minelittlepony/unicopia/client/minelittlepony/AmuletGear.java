package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.IModel;
import com.minelittlepony.api.model.gear.IGear;
import com.minelittlepony.unicopia.client.render.AmuletFeatureRenderer.AmuletModel;
import com.minelittlepony.unicopia.item.AmuletItem;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

class AmuletGear extends AmuletModel implements IGear {

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    private IModel model;

    public AmuletGear() {
        super(AmuletModel.getData(new Dilation(0.3F)).createModel());
    }

    @Override
    public boolean canRender(IModel model, Entity entity) {
        return entity instanceof LivingEntity living && !AmuletItem.getForEntity(living).isEmpty();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.BODY;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return textures.computeIfAbsent(Registries.ITEM.getId(AmuletItem.getForEntity((LivingEntity)entity).getItem()), id -> new Identifier(id.getNamespace(), "textures/models/armor/" + id.getPath() + ".png"));
    }

    @Override
    public void setModelAttributes(IModel model, Entity entity) {
        this.model = model;

        if (model instanceof BipedEntityModel<?> biped) {
            setAngles((LivingEntity)entity, biped);
        }
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, UUID interpolatorId) {
        BangleGear.popAndApply(model, BodyPart.BODY, stack);

        stack.translate(0, 0.25, 0);

        render(stack, consumer, light, overlay, red, green, blue, 1);
    }
}
