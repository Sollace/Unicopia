package com.minelittlepony.unicopia.client.render;

import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.render.ModelPartHooks.EnqueudHeadRender;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.minelittlepony.unicopia.util.Untyped;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class WorldRenderDelegate {
    public static final WorldRenderDelegate INSTANCE = new WorldRenderDelegate();
    private static final Optional<Vec3d> RED_SKY_COLOR = Optional.of(new Vec3d(1, 0, 0));

    private final EntityReplacementManager disguiseLookup = new EntityReplacementManager();
    private final EntityDisguiseRenderer disguiseRenderer = new EntityDisguiseRenderer(this);
    private final SmittenEyesRenderer smittenEyesRenderer = new SmittenEyesRenderer();
    private final FishBowlRenderer fishBowlRenderer = new FishBowlRenderer();

    private static final int[] DEFAULT_FOG_DIFF = { 0, 0 };
    private static final int[] SEAPONY_UNDERWATER_FOG_DIFF = { -30, 190 };
    private static final int[] SEAPONY_SURFACE_FOG_DIFF = { -130, 0 };

    final MinecraftClient client = MinecraftClient.getInstance();

    public Optional<Vec3d> getSkyColor(float tickDelta) {
        if (EquinePredicates.RAGING.test(client.player)) {
            return RED_SKY_COLOR;
        }
        return Optional.empty();
    }

    public void applyFog(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta) {
        if (EquinePredicates.PLAYER_SEAPONY.test(MinecraftClient.getInstance().player)) {
            final int[] distanceChange = switch (camera.getSubmersionType()) {
                case WATER -> SEAPONY_UNDERWATER_FOG_DIFF;
                case NONE -> SEAPONY_SURFACE_FOG_DIFF;
                default -> DEFAULT_FOG_DIFF;
            };

            if (distanceChange != DEFAULT_FOG_DIFF) {
                final Fog fog = RenderSystem.getShaderFog();
                RenderSystem.setShaderFog(new Fog(
                    fog.start() + distanceChange[0], fog.end() + distanceChange[1],
                    fog.shape(),
                    fog.red(), fog.green(), fog.blue(), fog.alpha()
                ));
            }
        }
    }

    public <E extends Entity, S extends EntityRenderState> void handleEntityRender(
            EntityRenderRedispatcher<Entity> dispatcher,
            E entity,
            double x,
            double y,
            double z,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertices,
            int light) {
        @Nullable final Equine<?> equine = Equine.of(entity).orElse(null);
        @Nullable final Entity flippedEntity = equine != null && equine.getPhysics().isGravityNegative() ? entity : null;

        matrices.push();

        if (equine instanceof Living living && !living.isBeingCarried()) {
            applyTransforms(living, entity, x, y, z, matrices);
        }

        if (flippedEntity != null) {
            if (equine instanceof ItemImpl) {
                matrices.translate(0, -flippedEntity.getHeight() * 1.1, 0);
            }
            flipAngles(entity);
        }

        var disguise = equine instanceof Living living ? disguiseLookup.getAppearanceFor(living).orElse(null) : null;

        if (equine instanceof Living living) {
            Entity replacement = disguiseRenderer.prepare(living, disguise, x, y, z, tickDelta, matrices, vertices, light);
            if (replacement != null) {
                entity = Untyped.cast(replacement);
            }
        }

        final boolean hasSmittenEyes = equine instanceof Creature creature && smittenEyesRenderer.isSmitten(creature);
        final boolean hasFishbowl = fishBowlRenderer.shouldRender(equine);

        if (hasSmittenEyes || hasFishbowl) {
            ModelPartHooks.startCollecting();
        }

        if (disguise == null) {
            dispatcher.render(entity, x, y, z, applyOverlays(entity, vertices), light);
        } else {
            disguiseRenderer.render(dispatcher, disguise.getAppearance(), x, y, z, tickDelta, matrices, vertices, light);
        }

        Set<EnqueudHeadRender> headParts = ModelPartHooks.stopCollecting();

        if (hasSmittenEyes) {
            smittenEyesRenderer.render(headParts, entity, matrices, vertices, light);
        }

        if (hasFishbowl) {
            fishBowlRenderer.render(headParts, matrices, vertices, light);
        }

        matrices.pop();

        if (flippedEntity != null) {
            flipAngles(flippedEntity);
        }
    }

    public VertexConsumerProvider applyOverlays(Entity entity, VertexConsumerProvider vertices) {
        if (entity instanceof BoatEntity && entity instanceof LavaAffine affine && affine.isLavaAffine()) {
            Identifier frostingTexture = Unicopia.id("textures/entity/" + EntityType.getId(entity.getType()).getPath() + "/frosting.png");
            vertices = RenderLayerUtil.createUnionBuffer(vertices, texture -> RenderLayer.getEntityTranslucent(frostingTexture));
        }

        if (Equine.of(entity).orElse(null) instanceof Creature creature && creature.isMinion()) {
            vertices = RenderLayerUtil.createUnionBuffer(vertices, texture -> RenderLayers.getMagicColored(texture, creature.isDiscorded() ? 0x33FF0000 : ColorHelper.getRainbowColor(creature.asEntity(), 25, 1) )); // 0x8800AA00
        }

        return vertices;
    }

    private void applyTransforms(Equine<?> equine, Entity entity, double x, double y, double z, MatrixStack matrices) {
        boolean negative = equine.getPhysics().isGravityNegative();

        matrices.translate(x, y + entity.getHeight() / 2, z);

        if (equine instanceof Pony p) {
            boolean firstPerson = client.options.getPerspective().isFirstPerson();
            float fovEffectScale = client.options.getFovEffectScale().getValue().floatValue();
            float sidewaysRoll = p.getCamera().calculateRoll(firstPerson, fovEffectScale);

            if (p.getAcrobatics().isFloppy()) {
                matrices.translate(0, -0.5, 0);
                p.asEntity().setBodyYaw(0);
                p.asEntity().setYaw(0);
                sidewaysRoll += 90;
            }

            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sidewaysRoll));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));

            float forwardPitch = p.getInterpolator().interpolate("g_kdive", p.getMotion().isDiving() ? 80 : 0, 15);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(forwardPitch));
        } else {
            float roll = negative ? 180 : 0;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        }

        matrices.translate(-x, -y - entity.getHeight() / 2, -z);
    }

    private void flipAngles(Entity entity) {
        if (entity instanceof PlayerEntity) {
            entity.lastYaw *= -1;
            entity.setYaw(entity.getYaw() * -1);

            entity.lastPitch *= -1;
            entity.setPitch(entity.getPitch() * -1);
        }

        if (entity instanceof LivingEntity living) {
            living.bodyYaw = -living.bodyYaw;
            living.lastBodyYaw = -living.lastBodyYaw;
            living.headYaw = -living.headYaw;
            living.lastHeadYaw = -living.lastHeadYaw;
        }
    }
}
