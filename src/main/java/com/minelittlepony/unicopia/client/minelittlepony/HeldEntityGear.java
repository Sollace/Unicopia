package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.client.render.HeldEntityFeatureRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

class HeldEntityGear extends HeldEntityFeatureRenderer<LivingEntity> implements Gear {

    private LivingEntity entity;

    public HeldEntityGear() {
        super(null);
    }

    @Override
    public boolean canRender(PonyModel<?> model, Entity entity) {
        return entity instanceof LivingEntity;
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.BODY;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity).getTexture(entity);
    }

    @Override
    public <M extends EntityModel<?> & PonyModel<?>> void transform(M model, MatrixStack matrices) {
        // noop
    }

    @Override
    public void pose(PonyModel<?> model, Entity entity, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        this.entity = (LivingEntity)entity;
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha, UUID interpolatorId) {
        render(
            stack,
            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
            light, entity,
            0, 0,
            MinecraftClient.getInstance().getTickDelta(), 0, 0, 0
        );
    }
}
