package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.api.pony.meta.Wearable;
import com.minelittlepony.unicopia.client.render.HeldEntityFeatureRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

class HeldEntityGear extends HeldEntityFeatureRenderer<BipedEntityRenderState, LivingEntity> implements Gear {

    private BipedEntityRenderState entity;

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
        return context.getDefaultTexture(entity, Wearable.NONE);
    }

    @Override
    public <M extends EntityModel<?> & PonyModel<?>> void transform(M model, MatrixStack matrices) {
        // noop
    }

    @Override
    public void pose(PonyModel<?> model, Entity entity, boolean rainboom, UUID interpolatorId, float move, float swing, float bodySwing, float ticks) {
        this.entity = (BipedEntityRenderState)MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity).getAndUpdateRenderState(entity, ticks);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer buffer, int light, int overlay, int color, UUID interpolatorId) {
        render(
            matrices,
            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
            light, entity,
            0, 0
        );
    }
}
