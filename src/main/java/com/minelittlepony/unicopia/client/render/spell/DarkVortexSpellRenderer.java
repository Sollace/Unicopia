package com.minelittlepony.unicopia.client.render.spell;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.DarkVortexSpell;
import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.client.render.entity.state.CasterState;
import com.minelittlepony.unicopia.client.render.model.PlaneModel;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.client.render.spell.SpellRenderer.SpellRenderState;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class DarkVortexSpellRenderer extends SpellRenderer<DarkVortexSpell, DarkVortexSpellRenderer.State> {
    private static final Identifier ACCRETION_DISK_TEXTURE = Unicopia.id("textures/spells/dark_vortex/accretion_disk.png");
    private static final int DISTORTION_ZONE_COLOR = ColorHelper.withAlpha(Colors.BLACK, (int)(255 * 0.9F));

    private static float cameraDistortion;

    public static float getCameraDistortion() {
        cameraDistortion *= 0.9F;
        cameraDistortion = MathHelper.clamp(cameraDistortion, 0, 80);
        return cameraDistortion;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(DarkVortexSpell spell, State state, Caster<?> caster, float tickDelta) {
        super.updateRenderState(spell, state, caster, tickDelta);
        state.radius = (float)spell.getEventHorizonRadius();
        state.range = (float)spell.getDrawDropOffRange() / 8F;
        state.yOffset = spell.getYOffset();
        state.origin = spell.getOrigin(caster);
    }

    public static class State extends SpellRenderState {
        public float radius;
        public float range;
        public double yOffset;

        public Vec3d origin = Vec3d.ZERO;
    }

    @Override
    public boolean shouldRenderEffectPass(int pass) {
        return pass < 2;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertices, State state, CasterState caster, int light) {
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();

        Vec3d ray = camera.getPos().subtract(state.origin);

        float absDistance = (float)ray.length();

        matrices.push();
        matrices.translate(0, state.yOffset, 0);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(caster.yawOffset));

        float visualRadius = Math.min(state.radius * 0.8F, absDistance - 1F);

        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayer.getSolid()), light, 1, visualRadius, Colors.BLACK);
        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getMagicColored()), light, 1, visualRadius + 0.05F, DISTORTION_ZONE_COLOR);
        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getMagicColored()), light, 1, visualRadius + 0.1F, DISTORTION_ZONE_COLOR);
        SphereModel.SPHERE.render(matrices, vertices.getBuffer(RenderLayers.getMagicColored()), light, 1, visualRadius + 0.15F, DISTORTION_ZONE_COLOR);

        matrices.push();

        float distance = 1F / MathHelper.clamp(absDistance / (state.radius + 7), 0.0000001F, 1);
        distance *= distance;
        if (absDistance < state.radius * 4) {
            cameraDistortion += distance;
        }

        SphereModel.DISK.render(matrices, vertices.getBuffer(RenderLayer.getEndPortal()), light, 1, state.radius * 0.5F, 0);

        if (state.radius > 0.3F && absDistance > state.radius) {
            double g = Math.sqrt(ray.x * ray.x + ray.z * ray.z);
            float age = caster.entityState.age;
            float pitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(ray.y, g) * 180.0F / (float)Math.PI)));
            float yaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(ray.z, ray.x) * 180.0F / (float)Math.PI) - 90.0F);

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-caster.yawOffset - yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-pitch));

            float processionSpeed = age * 0.02F;
            float maxProcessionAngle = 15;

            float cosProcession = MathHelper.cos(processionSpeed);
            float sinProcession = MathHelper.sin(processionSpeed);

            matrices.scale(state.range, state.range, state.range);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(cosProcession * maxProcessionAngle));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sinProcession * maxProcessionAngle));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(age * 18));

            VertexConsumer buffer = vertices.getBuffer(RenderLayer.getEntityTranslucent(ACCRETION_DISK_TEXTURE));

            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, Colors.WHITE);
            float secondaryScale = 0.9F + cosProcession * 0.3F;
            matrices.translate(0, 0, 0.0001F);
            matrices.scale(secondaryScale, secondaryScale, secondaryScale);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(33));
            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, Colors.WHITE);
            matrices.translate(0, 0, 0.0001F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(33));
            PlaneModel.INSTANCE.render(matrices, buffer, light, 0, 1, Colors.WHITE);
        }
        matrices.pop();
        matrices.pop();
    }

    @Override
    protected void renderCountdown(MatrixStack matrices, TimedSpell spell, float tickDelta) {

    }
}
