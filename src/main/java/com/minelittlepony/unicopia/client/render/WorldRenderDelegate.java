package com.minelittlepony.unicopia.client.render;

import java.util.Optional;

import com.minelittlepony.client.util.render.RenderLayerUtil;
import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

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

    public boolean beforeEntityRender(Entity entity,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!recurseFrosting && entity instanceof BoatEntity && entity instanceof LavaAffine affine && affine.isLavaAffine()) {
            Identifier frostingTexture = Unicopia.id("textures/entity/" + EntityType.getId(entity.getType()).getPath() + "/frosting.png");

            if (MinecraftClient.getInstance().getResourceManager().getResource(frostingTexture).isPresent()) {
                recurseFrosting = true;
                client.getEntityRenderDispatcher().render(entity, x, y, z, yaw, tickDelta, matrices, layer -> {
                    if (RenderLayerUtil.getTexture(layer).orElse(null) == null) {
                        return vertices.getBuffer(layer);
                    }
                    return VertexConsumers.union(vertices.getBuffer(layer), vertices.getBuffer(RenderLayers.getEntityTranslucent(frostingTexture)));
                }, light);
                recurseFrosting = false;
                return true;
            }
        }

        if (recurseFrosting) {
            return false;
        }

        return Equine.of(entity).filter(eq -> onEntityRender(eq, x, y, z, yaw, tickDelta, matrices, vertices, light)).isPresent();
    }

    public void afterEntityRender(Equine<?> pony, MatrixStack matrices, int light) {
        if (recurseFrosting) {
            return;
        }

        if (pony instanceof Creature creature && smittenEyesRenderer.isSmitten(creature)) {
            Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            smittenEyesRenderer.render(creature, matrices, immediate, light, 0);
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
                client.getEntityRenderDispatcher().render(creature.asEntity(), x, y, z, yaw, tickDelta, matrices, layer -> {
                    return RenderLayerUtil.getTexture(layer)
                            .filter(texture -> texture != PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                            .map(texture -> {
                        return VertexConsumers.union(
                                vertices.getBuffer(layer),
                                vertices.getBuffer(RenderLayers.getMagicColored(texture, creature.isDiscorded() ? 0xCCFF0000 : 0xCC0000FF))
                        );
                    }).orElseGet(() -> vertices.getBuffer(layer));
                }, light);
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

        float roll = negative ? 180 : 0;

        roll = pony instanceof Pony ? ((Pony)pony).getInterpolator().interpolate("g_roll", roll, 15) : roll;

        matrices.translate(x, y + owner.getHeight() / 2, z);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(roll));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(roll));

        if (pony instanceof Pony p) {
            roll = p.getCamera().calculateRoll();
            if (negative) {
                roll -= 180;
            }

            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));

            float diveAngle = p.getInterpolator().interpolate("g_kdive", p.getMotion().isDiving() ? 80 : 0, 15);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(diveAngle));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        } else if (pony instanceof Creature creature && smittenEyesRenderer.isSmitten(creature)) {
            ModelPartHooks.startCollecting();
        }

        matrices.translate(-x, -y - owner.getHeight() / 2, -z);

        if (negative) {
            flipAngles(owner);
        }

        return disguiseLookup.getAppearanceFor(pony).map(effect -> disguiseRenderer.render(pony, effect, x, y, z, tickDelta, matrices, vertexConsumers, light)).orElse(false);
    }

    private void flipAngles(Entity entity) {
        if (entity instanceof PlayerEntity) {
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
