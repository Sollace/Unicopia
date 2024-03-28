package com.minelittlepony.unicopia.client.render;

import java.util.Optional;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.minelittlepony.MineLPDelegate;
import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.BackgroundRenderer.FogType;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
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

    private boolean recurseMinion;
    private boolean recurseFrosting;

    final MinecraftClient client = MinecraftClient.getInstance();

    public Optional<Vec3d> getSkyColor(float tickDelta) {
        if (EquinePredicates.RAGING.test(client.player)) {
            return RED_SKY_COLOR;
        }
        return Optional.empty();
    }

    public void applyFog(Camera camera, FogType fogType, float viewDistance, boolean thickFog, float tickDelta) {
        if (camera.getSubmersionType() == CameraSubmersionType.WATER) {
            if (EquinePredicates.PLAYER_SEAPONY.test(MinecraftClient.getInstance().player)) {
                RenderSystem.setShaderFogStart(RenderSystem.getShaderFogStart() - 30);
                RenderSystem.setShaderFogEnd(RenderSystem.getShaderFogEnd() + 190);
            }
        }
        if (camera.getSubmersionType() == CameraSubmersionType.NONE) {
            if (EquinePredicates.PLAYER_SEAPONY.test(MinecraftClient.getInstance().player)) {
                RenderSystem.setShaderFogStart(-130);
            }
        }
    }

    public boolean beforeEntityRender(Entity entity,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!recurseFrosting && entity instanceof BoatEntity && entity instanceof LavaAffine affine && affine.isLavaAffine()) {
            Identifier frostingTexture = Unicopia.id("textures/entity/" + EntityType.getId(entity.getType()).getPath() + "/frosting.png");

            if (MinecraftClient.getInstance().getResourceManager().getResource(frostingTexture).isPresent()) {
                recurseFrosting = true;
                RenderLayerUtil.createUnionBuffer(c -> {
                    client.getEntityRenderDispatcher().render(entity, x, y, z, yaw, tickDelta, matrices, c, light);
                }, vertices, texture -> RenderLayers.getEntityTranslucent(frostingTexture));
                recurseFrosting = false;
                return true;
            }
        }

        if (recurseFrosting) {
            return false;
        }

        return Equine.of(entity).filter(eq -> onEntityRender(eq, x, y, z, yaw, tickDelta, matrices, vertices, light)).isPresent();
    }

    public void afterEntityRender(Equine<?> pony, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        if (recurseFrosting) {
            return;
        }

        if (pony instanceof Creature creature && smittenEyesRenderer.isSmitten(creature)) {
            smittenEyesRenderer.render(creature, matrices, vertices, light, 0);
        }

        if (pony instanceof Pony p) {
            if (p.getCompositeRace().includes(Race.SEAPONY)
                    && pony.asEntity().isSubmergedInWater()
                    && MineLPDelegate.getInstance().getPlayerPonyRace(p.asEntity()) != Race.SEAPONY) {

                for (var head : ModelPartHooks.stopCollecting()) {
                    matrices.push();
                    head.transform(matrices, 1F);

                    Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
                    RenderLayer layer = RenderLayers.getMagicColored();
                    float scale = 0.9F;

                    SphereModel.SPHERE.render(matrices, immediate.getBuffer(layer), light, 0, scale, 0.5F, 0.5F, 0.5F, 0.1F);
                    SphereModel.SPHERE.render(matrices, immediate.getBuffer(layer), light, 0, scale + 0.2F, 0.5F, 0.5F, 0.5F, 0.1F);

                    matrices.pop();
                }
            }
        }

        if (pony instanceof ItemImpl || pony instanceof Living) {
            matrices.pop();

            if (pony instanceof Living && pony.getPhysics().isGravityNegative()) {
                flipAngles(pony.asEntity());
            }
        }
    }

    private boolean onEntityRender(Equine<?> pony,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!recurseMinion && pony instanceof Creature creature && creature.isMinion()) {
            try {
                recurseMinion = true;
                RenderLayerUtil.createUnionBuffer(c -> {
                    client.getEntityRenderDispatcher().render(creature.asEntity(), x, y, z, yaw, tickDelta, matrices, c, light);
                }, vertices, texture -> RenderLayers.getMagicColored(texture, creature.isDiscorded() ? 0x33FF0000 : ColorHelper.getRainbowColor(creature.asEntity(), 25, 1) )); // 0x8800AA00
                return true;
            } catch (Throwable t) {
                Unicopia.LOGGER.error("Error whilst rendering minion", t);
            } finally {
                recurseMinion = false;
            }
        }

        if (pony instanceof ItemImpl) {
            matrices.push();

            if (pony.getPhysics().isGravityNegative()) {
                matrices.translate(0, -((ItemImpl) pony).asEntity().getHeight() * 1.1, 0);
            }

            return false;
        }

        if (pony instanceof Living living) {
            return onLivingRender(living, x, y, z, yaw, tickDelta, matrices, vertices, light);
        }

        return false;
    }

    private boolean onLivingRender(Living<?> pony,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        if (pony.isBeingCarried()) {
            return true;
        }

        pony.updateSupportingEntity();

        matrices.push();

        Entity owner = pony.asEntity();

        boolean negative = pony.getPhysics().isGravityNegative();

        matrices.translate(x, y + owner.getHeight() / 2, z);

        if (pony instanceof Pony p) {
            float sidewaysRoll = p.getCamera().calculateRoll();

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

            if (p.getCompositeRace().includes(Race.SEAPONY)
                    && pony.asEntity().isSubmergedInWater()
                    && MineLPDelegate.getInstance().getPlayerPonyRace(p.asEntity()) != Race.SEAPONY) {
                ModelPartHooks.startCollecting();
            }
        } else {
            float roll = negative ? 180 : 0;

            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));

            if (pony instanceof Creature creature && smittenEyesRenderer.isSmitten(creature)) {
                ModelPartHooks.startCollecting();
            }
        }

        matrices.translate(-x, -y - owner.getHeight() / 2, -z);

        if (negative) {
            flipAngles(owner);
        }

        return disguiseLookup.getAppearanceFor(pony).map(effect -> disguiseRenderer.render(pony, effect, x, y, z, tickDelta, matrices, vertexConsumers, light)).orElse(false);
    }

    private void flipAngles(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            entity.prevYaw *= -1;
            entity.setYaw(entity.getYaw() * -1);

            entity.prevPitch *= -1;
            entity.setPitch(entity.getPitch() * -1);
        }

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;

            living.bodyYaw = -living.bodyYaw;
            living.prevBodyYaw = -living.prevBodyYaw;
            living.headYaw = -living.headYaw;
            living.prevHeadYaw = -living.prevHeadYaw;
        }
    }
}
