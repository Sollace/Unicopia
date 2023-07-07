package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.entity.Creature;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.entity.behaviour.FallingBlockBehaviour;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class WorldRenderDelegate {
    public static final WorldRenderDelegate INSTANCE = new WorldRenderDelegate();

    private static final PassThroughVertexConsumer.Parameters MINION_OVERLAY = new PassThroughVertexConsumer.Parameters()
            .color((parent, r, g, b, a) -> parent.color((float)Math.random(), 0.6F, 1, a / 255F));

    private boolean recurseMinion;
    private boolean recurseFrosting;

    public boolean beforeEntityRender(EntityRenderDispatcher dispatcher, Entity entity,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!recurseFrosting && entity instanceof BoatEntity && entity instanceof LavaAffine affine && affine.isLavaAffine()) {
            Identifier frostingTexture = Unicopia.id("textures/entity/" + EntityType.getId(entity.getType()).getPath() + "/frosting.png");

            if (MinecraftClient.getInstance().getResourceManager().getResource(frostingTexture).isPresent()) {
                recurseFrosting = true;

                dispatcher.render(entity, x, y, z, yaw, tickDelta, matrices, vertices, light);
                dispatcher.setRenderShadows(false);
                dispatcher.render(entity, x, y, z, yaw, tickDelta, matrices, layer -> {
                    return vertices.getBuffer(RenderLayers.getEntityTranslucent(frostingTexture));
                }, light);
                dispatcher.setRenderShadows(true);
                recurseFrosting = false;
                return true;
            }
        }

        if (recurseFrosting) {
            return false;
        }

        return Equine.of(entity).filter(eq -> onEntityRender(dispatcher, eq, x, y, z, yaw, tickDelta, matrices, vertices, light)).isPresent();
    }

    public boolean onEntityRender(EntityRenderDispatcher dispatcher, Equine<?> pony,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!recurseMinion && pony instanceof Creature creature && creature.isMinion()) {
            recurseMinion = true;
            dispatcher.render(creature.asEntity(), x, y, z, yaw, tickDelta, matrices, layer -> {
                return PassThroughVertexConsumer.of(vertices.getBuffer(layer), MINION_OVERLAY);
            }, light);
            recurseMinion = false;

            return true;
        }

        if (pony instanceof ItemImpl) {
            matrices.push();

            if (pony.getPhysics().isGravityNegative()) {
                matrices.translate(0, -((ItemImpl) pony).asEntity().getHeight() * 1.1, 0);
            }

            return false;
        }

        if (pony instanceof Living) {
            return onLivingRender(dispatcher, (Living<?>)pony, x, y, z, yaw, tickDelta, matrices, vertices, light);
        }

        return false;
    }

    private boolean onLivingRender(EntityRenderDispatcher dispatcher, Living<?> pony,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        if (pony.isBeingCarried() && !(pony instanceof Pony && ((Pony)pony).isClientPlayer())) {
            return true;
        }

        matrices.push();

        Entity owner = pony.asEntity();

        boolean negative = pony.getPhysics().isGravityNegative();

        float roll = negative ? 180 : 0;

        roll = pony instanceof Pony ? ((Pony)pony).getInterpolator().interpolate("g_roll", roll, 15) : roll;

        matrices.translate(x, y + owner.getHeight() / 2, z);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(roll));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(roll));

        if (pony instanceof Pony) {
            roll = ((Pony)pony).getCamera().calculateRoll();
            if (negative) {
                roll -= 180;
            }

            matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(yaw));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
        }

        matrices.translate(-x, -y - owner.getHeight() / 2, -z);

        if (negative) {
            flipAngles(owner);
        }

        if (pony instanceof Caster<?>) {
            int fireTicks = owner.doesRenderOnFire() ? 1 : 0;

            return ((Caster<?>)pony).getSpellSlot().get(SpellPredicate.IS_DISGUISE, false).map(effect -> {
                effect.update(pony, false);

                EntityAppearance ve = effect.getDisguise();
                Entity e = ve.getAppearance();

                if (e != null) {
                    renderDisguise(dispatcher, ve, e, x, y, z, fireTicks, tickDelta, matrices, vertexConsumers, light);
                    ve.getAttachments().forEach(ee -> {
                        Vec3d difference = ee.getPos().subtract(e.getPos());
                        renderDisguise(dispatcher, ve, ee, x + difference.x, y + difference.y, z + difference.z, fireTicks, tickDelta, matrices, vertexConsumers, light);
                    });

                    afterEntityRender(pony, matrices);
                    return true;
                }
                return false;
            }).orElse(false);
        }

        return false;
    }

    public void afterEntityRender(Equine<?> pony, MatrixStack matrices) {
        if (recurseFrosting) {
            return;
        }

        if (pony instanceof ItemImpl || pony instanceof Living) {
            matrices.pop();

            if (pony instanceof Living && pony.getPhysics().isGravityNegative()) {
                flipAngles(pony.asEntity());
            }
        }
    }

    public void renderDisguise(EntityRenderDispatcher dispatcher, EntityAppearance ve, Entity e,
            double x, double y, double z,
            int fireTicks, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        if (ve.isAxisAligned() && (x != 0 || y != 0 || z != 0)) {
            Vec3d cam = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();

            x = MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()) - cam.x;
            y = MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()) - cam.y;
            z = MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()) - cam.z;
        }

        BlockEntity blockEntity = ve.getBlockEntity();

        if (blockEntity != null) {
            BlockEntityRenderer<BlockEntity> r = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(blockEntity);
            if (r != null) {
                ((FallingBlockBehaviour.Positioned)blockEntity).setPos(e.getBlockPos());
                blockEntity.setWorld(e.getWorld());
                matrices.push();

                BlockState state = blockEntity.getCachedState();
                Direction direction = state.contains(Properties.HORIZONTAL_FACING) ? state.get(Properties.HORIZONTAL_FACING) : Direction.UP;

                matrices.translate(x, y, z);

                matrices.multiply(direction.getRotationQuaternion());
                matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(90));

                matrices.translate(-0.5, 0, -0.5);

                r.render(blockEntity, 1, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

                matrices.pop();
                blockEntity.setWorld(null);
                return;
            }
        }

        BipedEntityModel<?> model = getBipedModel(dispatcher, e);

        if (model != null) {
            model.sneaking = e.isSneaking();
        }

        e.setFireTicks(fireTicks);
        dispatcher.render(e, x, y, z, e.getYaw(), tickDelta, matrices, vertexConsumers, light);
        e.setFireTicks(0);

        if (model != null) {
            model.sneaking = false;
        }
    }

    @Nullable
    private BipedEntityModel<?> getBipedModel(EntityRenderDispatcher dispatcher, Entity entity) {
        EntityRenderer<?> renderer = dispatcher.getRenderer(entity);
        if (renderer instanceof LivingEntityRenderer) {
            Model m = ((LivingEntityRenderer<?, ?>) renderer).getModel();
            if (m instanceof BipedEntityModel<?>) {
                return (BipedEntityModel<?>)m;
            }
        }
        return null;
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
