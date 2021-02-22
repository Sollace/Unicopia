package com.minelittlepony.unicopia.client.render;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.DisguiseSpell;
import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.ItemImpl;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WorldRenderDelegate {
    public static final WorldRenderDelegate INSTANCE = new WorldRenderDelegate();

    public boolean onEntityRender(EntityRenderDispatcher dispatcher, Equine<?> pony,
            double x, double y, double z, float yaw,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (pony instanceof ItemImpl) {
            matrices.push();

            if (pony.getPhysics().isGravityNegative()) {
                matrices.translate(0, -((ItemImpl) pony).getMaster().getHeight() * 1.1, 0);
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

        matrices.push();

        Entity owner = pony.getMaster();

        boolean negative = pony.getPhysics().isGravityNegative();

        float roll = negative ? 180 : 0;

        roll = pony instanceof Pony ? ((Pony)pony).getInterpolator().interpolate("g_roll", roll, 15) : roll;

        if (negative) {
            matrices.translate(x, y, z);
            matrices.translate(0, owner.getHeight(), 0);
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(roll));
            matrices.translate(-x, -y, -z);

            flipAngles(owner);
        } else {
            matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(roll));
        }

        if (pony instanceof Caster<?>) {

            int fireTicks = pony.getMaster().doesRenderOnFire() ? 1 : 0;

            return ((Caster<?>)pony).getSpellOrEmpty(DisguiseSpell.class, true).map(effect -> {
                effect.update(pony, false);

                Disguise ve = effect.getDisguise();
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

        if (pony instanceof ItemImpl || pony instanceof Living) {
            matrices.pop();

            if (pony instanceof Living && pony.getPhysics().isGravityNegative()) {
                flipAngles(((Living<?>)pony).getMaster());
            }
        }
    }

    public void renderDisguise(EntityRenderDispatcher dispatcher, Disguise ve, Entity e,
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
            BlockEntityRenderer<BlockEntity> r = BlockEntityRenderDispatcher.INSTANCE.get(blockEntity);
            if (r != null) {
                blockEntity.setPos(e.getBlockPos());
                matrices.push();

                BlockState state = blockEntity.getCachedState();
                Direction direction = state.contains(Properties.HORIZONTAL_FACING) ? state.get(Properties.HORIZONTAL_FACING) : Direction.UP;

                matrices.translate(x, y, z);

                matrices.multiply(direction.getRotationQuaternion());
                matrices.multiply(Vector3f.NEGATIVE_X.getDegreesQuaternion(90));

                matrices.translate(-0.5, 0, -0.5);

                r.render(blockEntity, 1, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

                matrices.pop();
                return;
            }
        }

        BipedEntityModel<?> model = getBipedModel(dispatcher, e);

        if (model != null) {
            model.sneaking = e.isSneaking();
        }

        e.setFireTicks(fireTicks);
        dispatcher.render(e, x, y, z, e.yaw, tickDelta, matrices, vertexConsumers, light);
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
        if (!MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            entity.prevYaw *= -1;
            entity.yaw *= -1;

            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;

                living.bodyYaw = -living.bodyYaw;
                living.prevBodyYaw = -living.prevBodyYaw;
                living.headYaw = -living.headYaw;
                living.prevHeadYaw = -living.prevHeadYaw;
            }
        }
        entity.prevPitch *= -1;
        entity.pitch *= -1;
    }
}
