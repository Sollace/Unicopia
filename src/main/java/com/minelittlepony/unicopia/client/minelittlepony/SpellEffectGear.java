package com.minelittlepony.unicopia.client.minelittlepony;

import java.util.UUID;

import com.minelittlepony.api.model.BodyPart;
import com.minelittlepony.api.model.PonyModel;
import com.minelittlepony.api.model.gear.Gear;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.client.render.BraceletFeatureRenderer;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

class SpellEffectGear implements Gear {
    private CasterState caster;

    @Override
    public boolean canRender(PonyModel<?> model, Entity entity) {
        return Caster.of(entity).isPresent();
    }

    @Override
    public BodyPart getGearLocation() {
        return BodyPart.LEGS;
    }

    @Override
    public <T extends Entity> Identifier getTexture(T entity, Context<T, ?> context) {
        return BraceletFeatureRenderer.TEXTURE;
    }

    @Override
    public <M extends EntityModel<?> & PonyModel<?>> void transform(M model, MatrixStack matrices) {
    }

    @Override
    public void pose(PonyModel<?> model, Entity entity, boolean rainboom, UUID interpolatorId, float limbAngle, float limbDistance, float bodySwing, float tickDelta) {
        caster = CasterState.of(entity, tickDelta);
    }

    @Override
    public void render(MatrixStack stack, VertexConsumer consumer, int light, int overlay, int color, UUID interpolatorId) {
        SpellEffectsRenderDispatcher.INSTANCE.render(
            stack,
            MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
            light, caster
        );
    }
}
