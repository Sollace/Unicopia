package com.minelittlepony.unicopia.client.render.spell;

import java.util.UUID;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.EntityReference.EntityValues;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class PortalSpellRenderer extends SpellRenderer<PortalSpell, PortalSpellRenderer.State> {
    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass == 0;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(PortalSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.destination.copyFrom(spell.getDestinationReference());
        state.shouldRenderContents = true;
        state.yaw = spell.getYaw();
        state.portalUuid = caster.asEntity().getUuid();
        state.portalState = new EntityReference.EntityValues<>(caster.asEntity());
        state.orientationChange = spell.getOrientationChange();
        state.positionMatrix = spell.getPositionMatrix(caster, state.portalState.pos(), state.orientationChange, new Matrix4f());
        state.pitchChange = -spell.getTargetPitch() + spell.getPitch();
        state.yawChange = spell.getYawDifference();
        state.strength = 1 + MathHelper.sin(caster.asEntity().age + tickDelta) * 0.1F;

        if (client.cameraEntity instanceof CastSpellEntity) {
            double distance = caster.asEntity().distanceTo(client.cameraEntity);
            state.shouldRenderContents =
                    distance <= 50 // don't bother rendering if too far away
                    && distance >= 2; // don't render ourselves
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, State spell, CasterState caster, int light) {
        super.render(matrices, vertices, spell, caster, light);

        VertexConsumer buff = vertices.getBuffer(RenderLayers.getEndGateway());

        matrices.push();
        matrices.translate(0, 0.02, 0);
        SphereModel.DISK.render(matrices, buff, light, 0, 2F * spell.strength, Colors.WHITE);
        matrices.pop();

        if (Unicopia.getConfig().simplifiedPortals.get() || !spell.destination.isSet()) {
            matrices.push();
            matrices.translate(0, -0.02, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            SphereModel.DISK.render(matrices, buff, light, 0, 2F * spell.strength, Colors.WHITE);
            matrices.pop();
            return;
        }

        if (!spell.shouldRenderContents) {
            return;
        }

        matrices.push();
        matrices.scale(spell.strength, spell.strength, spell.strength);

        spell.destination.getTarget().ifPresent(target -> {
            float grown = Math.min(caster.entityState.age, 20) / 20F;
            matrices.push();
            matrices.translate(0, -0.01, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-spell.yaw));
            matrices.scale(grown, 1, grown);
            boolean inRange = MinecraftClient.getInstance().player.getPos().distanceTo(target.pos()) < MinecraftClient.getInstance().gameRenderer.getViewDistanceBlocks();

            PortalFrameBuffer buffer = PortalFrameBuffer.unpool(target.uuid());
            if (buffer != null) {
                if (inRange) {
                    buffer.build(spell, caster, target);
                }
                buffer.draw(matrices, vertices);
            }
            if (!inRange) {
                buffer = PortalFrameBuffer.unpool(spell.portalUuid);
                if (buffer != null) {
                    buffer.build(spell, caster, spell.portalState);
                }
            }
            matrices.pop();
        });

        matrices.pop();
    }

    static class State extends SpellRenderState {
        public boolean shouldRenderContents;
        public final EntityReference<Entity> destination = new EntityReference<>();

        public float yaw;
        public UUID portalUuid;
        public EntityValues<Entity> portalState;

        public Quaternionf orientationChange;
        public Matrix4f positionMatrix;

        public float pitchChange;
        public float yawChange;
        public float strength;
    }
}
