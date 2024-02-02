package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.PortalSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.EntityReference;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public class PortalSpellRenderer extends SpellRenderer<PortalSpell> {
    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass == 0;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, PortalSpell spell, Caster<?> caster, int light, float strength, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        super.render(matrices, vertices, spell, caster, light, strength, limbDistance, tickDelta, animationProgress, headYaw, headPitch);

        VertexConsumer buff = vertices.getBuffer(RenderLayers.getEndGateway());

        matrices.push();
        matrices.translate(0, 0.02, 0);
        SphereModel.DISK.render(matrices, buff, light, 0, 2F * strength, 1, 1, 1, 1);
        matrices.pop();

        if (Unicopia.getConfig().simplifiedPortals.get() || !spell.isLinked()) {
            matrices.push();
            matrices.translate(0, -0.02, 0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            SphereModel.DISK.render(matrices, buff, light, 0, 2F * strength, 1, 1, 1, 1);
            matrices.pop();
            return;
        }

        // Fancy portal rendering is disabled for now
        // Need to fix:
        // 1. Transparent parts of the sky (because the game sets the clear to (0,0,0,0)

        if (caster.asEntity().distanceTo(client.cameraEntity) > 50) {
            return; // don't bother rendering if too far away
        }
        if (client.cameraEntity == caster.asEntity()) {
            return;
        }

        matrices.push();
        matrices.scale(strength, strength, strength);

        spell.getTarget().ifPresent(target -> {
            float grown = Math.min(caster.asEntity().age, 20) / 20F;
            matrices.push();
            matrices.translate(0, -0.01, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-spell.getYaw()));
            matrices.scale(grown, 1, grown);
            boolean inRange = MinecraftClient.getInstance().player.getPos().distanceTo(target.pos()) < MinecraftClient.getInstance().gameRenderer.getViewDistance();

            PortalFrameBuffer buffer = PortalFrameBuffer.unpool(target.uuid());
            if (buffer != null) {
                if (inRange) {
                    buffer.build(spell, caster, target);
                }
                buffer.draw(matrices, vertices);
            }
            if (!inRange) {
                buffer = PortalFrameBuffer.unpool(caster.asEntity().getUuid());
                if (buffer != null) {
                    buffer.build(spell, caster, new EntityReference.EntityValues<>(caster.asEntity()));
                }
            }
            matrices.pop();
        });

        matrices.pop();
    }

    public interface WorldRendererDuck {
        ObjectArrayList<?> unicopia_getChunkInfos();

        Frustum unicopia_getFrustum();
    }
}
