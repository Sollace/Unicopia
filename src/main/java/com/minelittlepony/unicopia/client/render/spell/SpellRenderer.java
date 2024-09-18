package com.minelittlepony.unicopia.client.render.spell;

import org.joml.Quaternionf;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.util.ColorHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class SpellRenderer<T extends Spell> {
    public static final SpellRenderer<?> DEFAULT = new SpellRenderer<>();

    protected final MinecraftClient client = MinecraftClient.getInstance();

    public boolean shouldRenderEffectPass(int pass) {
        return true;
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertices, T spell, Caster<?> caster,  int light, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (caster.asEntity() == client.cameraEntity || (caster.asEntity() instanceof MagicProjectileEntity)) {
            return;
        }

        if (EquinePredicates.IS_CASTER.test(client.player)) {
            renderGemstone(matrices, vertices, spell, caster, light, tickDelta, animationProgress);
        }
    }

    private void renderGemstone(MatrixStack matrices, VertexConsumerProvider vertices, T spell, Caster<?> caster, int light, float tickDelta, float animationProgress) {
        matrices.push();
        float scale = 1/8F;
        matrices.scale(scale, scale, scale);

        transformGemstone(matrices, vertices, spell, caster, animationProgress);
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(animationProgress));

        client.getItemRenderer().renderItem(spell.getTypeAndTraits().getDefaultStack(), ModelTransformationMode.FIXED, light, 0, matrices, vertices, caster.asWorld(), 0);
        matrices.pop();

        if (spell instanceof TimedSpell timed) {
            if (caster.asEntity() instanceof LivingEntity l && !l.isInPose(EntityPose.SLEEPING)) {
                float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, l.prevBodyYaw, l.bodyYaw);
                float headYaw = MathHelper.lerpAngleDegrees(tickDelta, l.prevHeadYaw, l.headYaw);
                float yawDifference = headYaw - bodyYaw;
                if (l.hasVehicle() && l.getVehicle() instanceof LivingEntity vehicle) {
                    bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, vehicle.prevBodyYaw, vehicle.bodyYaw);
                    yawDifference = headYaw - bodyYaw;
                    float clampedYawDifference = MathHelper.clamp(MathHelper.wrapDegrees(yawDifference), -85, 85);
                    bodyYaw = headYaw - clampedYawDifference;
                    if (clampedYawDifference * clampedYawDifference > 2500) {
                        bodyYaw += clampedYawDifference * 0.2F;
                    }
                    yawDifference = headYaw - bodyYaw;
                }
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - bodyYaw));
            }
            renderCountdown(matrices, timed, tickDelta);
        }

        matrices.pop();
    }

    protected void renderCountdown(MatrixStack matrices, TimedSpell spell, float tickDelta) {
        matrices.multiply(client.getEntityRenderDispatcher().getRotation().invert(new Quaternionf()));
        float radius = 0.6F;
        float timeRemaining = spell.getTimer().getPercentTimeRemaining(tickDelta);

        DrawableUtil.drawArc(matrices, radius, radius + 0.3F, 0, DrawableUtil.TAU * timeRemaining,
                ColorHelper.lerp(MathHelper.clamp(timeRemaining * 4, 0, 1), 0xFF0000FF, 0xFFFFFFFF)
        );
    }

    protected void transformGemstone(MatrixStack matrices, VertexConsumerProvider vertices, T spell, Caster<?> caster, float animationProgress) {
        float y = -caster.asEntity().getHeight();
        if (caster.asEntity() instanceof CastSpellEntity) {
            y = 1F;
        }
        matrices.translate(0, y * 8 + MathHelper.sin(animationProgress / 3F) * 0.2F, 0);
    }
}
