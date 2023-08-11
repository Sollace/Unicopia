package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.client.util.render.RenderLayerUtil;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.compat.pehkui.PehkUtil;
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
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
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

    private boolean recurseMinion;
    private boolean recurseFrosting;

    public boolean beforeEntityRender(EntityRenderDispatcher dispatcher, Entity entity,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!recurseFrosting && entity instanceof BoatEntity && entity instanceof LavaAffine affine && affine.isLavaAffine()) {
            Identifier frostingTexture = Unicopia.id("textures/entity/" + EntityType.getId(entity.getType()).getPath() + "/frosting.png");

            if (MinecraftClient.getInstance().getResourceManager().getResource(frostingTexture).isPresent()) {
                recurseFrosting = true;
                dispatcher.render(entity, x, y, z, yaw, tickDelta, matrices, layer -> {
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

        return Equine.of(entity).filter(eq -> onEntityRender(dispatcher, eq, x, y, z, yaw, tickDelta, matrices, vertices, light)).isPresent();
    }

    public boolean onEntityRender(EntityRenderDispatcher dispatcher, Equine<?> pony,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!recurseMinion && pony instanceof Creature creature && creature.isMinion()) {
            try {
                recurseMinion = true;
                dispatcher.render(creature.asEntity(), x, y, z, yaw, tickDelta, matrices, layer -> {
                    var buffer = vertices.getBuffer(layer);
                    return RenderLayerUtil.getTexture(layer).map(texture -> {
                        return VertexConsumers.union(
                                vertices.getBuffer(RenderLayers.getMagicColored(texture, creature.isDiscorded() ? 0xCCFF0000 : 0xCC0000FF)),
                                vertices.getBuffer(layer)
                        );
                    }).orElse(buffer);
                }, light);
            } finally {
                recurseMinion = false;
            }
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

        if (pony.isBeingCarried()) {
            return true;
        }

        pony.updateRelativePosition();

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
                    PehkUtil.copyScale(pony.asEntity(), e);

                    if (dispatcher.shouldRenderHitboxes()) {
                        e.setBoundingBox(pony.asEntity().getBoundingBox());
                    }

                    renderDisguise(dispatcher, ve, e, x, y, z, fireTicks, tickDelta, matrices, vertexConsumers, light);
                    ve.getAttachments().forEach(ee -> {
                        PehkUtil.copyScale(pony.asEntity(), ee);
                        Vec3d difference = ee.getPos().subtract(e.getPos());
                        renderDisguise(dispatcher, ve, ee, x + difference.x, y + difference.y, z + difference.z, fireTicks, tickDelta, matrices, vertexConsumers, light);
                        PehkUtil.clearScale(ee);
                    });

                    afterEntityRender(pony, matrices);
                    PehkUtil.clearScale(e);
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
        if (dispatcher.getRenderer(entity) instanceof LivingEntityRenderer livingRenderer
              && livingRenderer.getModel() instanceof BipedEntityModel<?> biped) {
            return biped;
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
