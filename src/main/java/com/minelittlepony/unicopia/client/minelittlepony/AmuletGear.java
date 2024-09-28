package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.AmuletFeatureRenderer.AmuletModel;
import com.minelittlepony.unicopia.item.AmuletItem;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

class AmuletGear extends AmuletModel implements Gear {

    private final Map<Identifier, Identifier> textures = new HashMap<>();

    public AmuletGear() {
        super(AmuletModel.getData(new Dilation(0.3F)).createModel());
    }

    @Override
    public boolean canRender(PonyModel<?> model, Entity entity) {
        return entity instanceof LivingEntity living && !AmuletItem.get(living).stack().isEmpty();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.BODY;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return textures.computeIfAbsent(Registries.ITEM.getId(AmuletItem.get((LivingEntity)entity).stack().getItem()), id -> id.withPath(p  -> "textures/models/armor/" + p + ".png"));
    }

    @Override
    public <M extends EntityModel<?> & PonyModel<?>> void transform(M model, MatrixStack matrices) {
        BodyPart part = getGearLocation();
        model.transform(part, matrices);
        matrices.translate(0, 0.25, 0);
    }

    @Override
    public void pose(PonyModel<?> model, Entity entity, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        if (model instanceof BipedEntityModel<?> biped) {
            setAngles((LivingEntity)entity, biped);
        }
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        render(stack, consumer, light, overlay, color);
    }
}
