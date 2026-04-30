package com.minelittlepony.unicopia.client.render;

import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public Entity prepare(Living<?> pony, Disguise disguise,
            double x, double y, double z,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {

        if (!delegate.client.isPaused()) {
            disguise.update(pony, false);
        }

        EntityAppearance appearance = disguise.getAppearance();
        Entity e = appearance.getEntity();

        if (e == null) {
            return null;
        }

        BlockEntity blockEntity = appearance.getBlockEntity();
        if (blockEntity != null) {
            blockEntity.setWorld(e.getWorld());
        }

        int fireTicks = pony.asEntity().doesRenderOnFire() ? 1 : 0;

        if (delegate.client.getEntityRenderDispatcher().shouldRenderHitboxes()) {
            e.setBoundingBox(pony.asEntity().getBoundingBox());
        }
        e.setFireTicks(fireTicks);

        appearance.getAttachments().forEach(attachment -> {
            PehkUtil.copyScale(pony.asEntity(), attachment.entity());
            attachment.entity().setFireTicks(fireTicks);
        });

        PehkUtil.copyScale(pony.asEntity(), e);
        return e;
    }

    @Nullable
    public void render(EntityRenderRedispatcher<Entity> dispatcher, EntityAppearance appearance,
            double x, double y, double z,
            float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        Entity entity = appearance.getEntity();

        Vec3d cameraPos = delegate.client.gameRenderer.getCamera().getPos();

        if (appearance.isAxisAligned() && (x != 0 || y != 0 || z != 0)) {
            x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - cameraPos.x;
            y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - cameraPos.y;
            z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - cameraPos.z;
        }

        BlockEntity blockEntity = appearance.getBlockEntity();

        if (blockEntity == null || !renderBlockEntity(updateBlockEntity(blockEntity, entity), matrices, vertices, x, y, z, light, cameraPos)) {
            dispatcher.render(entity, x, y, z, delegate.applyOverlays(entity, vertices), light);
        }
        Vec3d origin = entity.getPos().subtract(x, y, z);
        appearance.getAttachments().forEach(attachment -> {
            Vec3d pos = attachment.entity().getPos().subtract(origin);
            if (blockEntity == null || !renderBlockEntity(updateBlockEntity(blockEntity, attachment.entity()), matrices, vertices, pos.x, pos.y, pos.z, light, cameraPos)) {
                dispatcher.render(entity, pos.x, pos.y, pos.z, vertices, light);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private BlockEntity updateBlockEntity(BlockEntity blockEntity, Entity entityReference) {
        ((MixinBlockEntity)blockEntity).setPos(entityReference.getBlockPos());
        if (entityReference instanceof FallingBlockEntity fbe) {
            blockEntity.setCachedState(fbe.getBlockState());
        }
        return blockEntity;
    }

    private boolean renderBlockEntity(BlockEntity blockEntity, MatrixStack matrices, VertexConsumerProvider vertices, double x, double y, double z, int light, Vec3d cameraPos) {
        var renderer = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(blockEntity);
        if (renderer != null) {
            matrices.push();
            matrices.translate(x - 0.5, y, z - 0.5);
            matrices.translate(-0.5, 0, -0.5);

            renderer.render(blockEntity, 1, matrices, vertices, light, OverlayTexture.DEFAULT_UV, cameraPos);

            matrices.pop();

            BlockRenderType type = blockEntity.getCachedState().getRenderType();
            if (type == BlockRenderType.INVISIBLE) {
                return false;
            }
        }

        return true;
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
