package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.client.render.spell.SpellEffectsRenderDispatcher;
import com.minelittlepony.unicopia.compat.pehkui.PehkUtil;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.Disguise;
import com.minelittlepony.unicopia.entity.behaviour.EntityAppearance;
import com.minelittlepony.unicopia.mixin.MixinBlockEntity;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

class EntityDisguiseRenderer {

    private final WorldRenderDelegate delegate;

    public EntityDisguiseRenderer(WorldRenderDelegate delegate) {
        this.delegate = delegate;
    }

    public boolean render(Living<?> pony, Disguise disguise,
            double x, double y, double z,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        int fireTicks = pony.asEntity().doesRenderOnFire() ? 1 : 0;
        disguise.update(pony, false);

        EntityAppearance ve = disguise.getDisguise();
        Entity e = ve.getAppearance();

        if (e == null) {
            return false;
        }

        PehkUtil.copyScale(pony.asEntity(), e);

        if (delegate.client.getEntityRenderDispatcher().shouldRenderHitboxes()) {
            e.setBoundingBox(pony.asEntity().getBoundingBox());
        }

        render(ve, e, x, y, z, fireTicks, tickDelta, matrices, vertices, light);
        ve.getAttachments().forEach(attachment -> {
            PehkUtil.copyScale(pony.asEntity(), attachment.entity());
            Vec3d difference = attachment.entity().getPos().subtract(e.getPos());
            render(ve, attachment.entity(), x + difference.x, y + difference.y, z + difference.z, fireTicks, tickDelta, matrices, vertices, light);
            PehkUtil.clearScale(attachment.entity());
        });

        matrices.push();
        matrices.translate(x, y, z);
        SpellEffectsRenderDispatcher.INSTANCE.render(matrices, vertices, light, pony, 0, 0, tickDelta, pony.asEntity().age + tickDelta, 0, 0);
        matrices.pop();

        delegate.afterEntityRender(pony, matrices, vertices, light);
        PehkUtil.clearScale(e);
        return true;
    }

    @SuppressWarnings("deprecation")
    private void render(EntityAppearance ve, Entity e,
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
                ((MixinBlockEntity)blockEntity).setPos(e.getBlockPos());
                if (e instanceof FallingBlockEntity fbe) {
                    blockEntity.setCachedState(fbe.getBlockState());
                }
                blockEntity.setWorld(e.getWorld());
                matrices.push();
                matrices.translate(x, y, z);
                matrices.translate(-0.5, 0, -0.5);

                r.render(blockEntity, 1, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);

                matrices.pop();

                BlockRenderType type = blockEntity.getCachedState().getRenderType();
                if (type == BlockRenderType.ENTITYBLOCK_ANIMATED) {
                    return;
                }
            }
        }

        BipedEntityModel<?> model = getBipedModel(e);

        if (model != null) {
            model.sneaking = e.isSneaking();
        }

        e.setFireTicks(fireTicks);

        EntityRenderDispatcher dispatcher = delegate.client.getEntityRenderDispatcher();
        if (e instanceof FallingBlockEntity) {
            dispatcher.setRenderShadows(false);
        }
        dispatcher.render(e, x, y, z, e.getYaw(), tickDelta, matrices, vertexConsumers, light);
        if (e instanceof FallingBlockEntity) {
            dispatcher.setRenderShadows(true);
        }
        e.setFireTicks(0);

        if (model != null) {
            model.sneaking = false;
        }
    }

    @Nullable
    static BipedEntityModel<?> getBipedModel(Entity entity) {
        if (MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer livingRenderer
              && livingRenderer.getModel() instanceof BipedEntityModel<?> biped) {
            return biped;
        }
        return null;
    }
}
