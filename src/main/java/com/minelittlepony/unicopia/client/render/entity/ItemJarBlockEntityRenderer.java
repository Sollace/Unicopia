package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.block.ItemJarBlock;
import com.minelittlepony.unicopia.block.ItemJarBlock.EntityJarContents;
import com.minelittlepony.unicopia.block.ItemJarBlock.ItemsJarContents;
import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.minelittlepony.unicopia.util.PosHelper;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ItemJarBlockEntityRenderer implements BlockEntityRenderer<ItemJarBlock.TileData> {

    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher dispatcher;

    private final Sprite waterSprite;

    public ItemJarBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        itemRenderer = ctx.getItemRenderer();
        dispatcher = ctx.getEntityRenderDispatcher();
        waterSprite = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
    }

    @Override
    public void render(ItemJarBlock.TileData data, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {

        ItemsJarContents items = data.getItems();
        if (items != null) {
            renderItemStacks(data, items, tickDelta, matrices, vertices, light, overlay);
        }

        EntityJarContents entity = data.getEntity();
        if (entity != null) {
            renderEntity(data, entity, tickDelta, matrices, vertices, light, overlay);
        }
    }

    private void renderItemStacks(ItemJarBlock.TileData data, ItemsJarContents items, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        float itemScale = 0.35F;

        matrices.push();
        matrices.translate(0.5, 0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.scale(itemScale, itemScale, itemScale);

        Random rng = Random.create(data.getPos().asLong());

        float y = 0;
        for (ItemStack stack : items.getStacks()) {
            matrices.push();

            matrices.translate((rng.nextFloat() - 0.5F) * 0.5F, (rng.nextFloat() - 0.5F) * 0.8F, -0.05 + y);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((rng.nextFloat() * 360) - 180));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((rng.nextFloat() * 360) - 180));
            y -= 0.1F;
            itemRenderer.renderItem(stack, ModelTransformationMode.FIXED, light, overlay, matrices, vertices, data.getWorld(), 0);
            matrices.pop();
        }
        matrices.pop();
    }

    private void renderEntity(ItemJarBlock.TileData data, EntityJarContents entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        Entity e = entity.getOrCreateEntity();
        if (e != null) {


            PlayerEntity player = MinecraftClient.getInstance().player;
            int age = player == null ? 0 : player.age;

            float fullTick = age + tickDelta;

            float size = Math.max(e.getWidth(), e.getHeight());
            float desiredSize = 0.25F;
            float scale = desiredSize / size;
            float eyePos = (e.getEyeHeight(e.getPose())) * scale;

            float yaw = 0;
            if (player != null) {
                Vec3d center = data.getPos().toCenterPos();
                Vec3d observerPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
                e.setPosition(center);
                e.lookAt(EntityAnchor.FEET, observerPos);
            }

            matrices.push();
            matrices.translate(0.5, 0.48 + MathHelper.sin(fullTick / 19F) * 0.02F - eyePos, 0.5);
            matrices.scale(scale, scale, scale);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10 * MathHelper.sin(fullTick / 19F)));
            dispatcher.render(e, 0, 0, 0, yaw * MathHelper.RADIANS_PER_DEGREE, tickDelta, matrices, vertices, light);
            matrices.pop();
        }
        renderFluid(data.getWorld(), data.getPos(), tickDelta, matrices, vertices, light, overlay);
    }

    private void renderFluid(World world, BlockPos pos, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.translate(0.3F, 0, 0.3F);
        RenderUtil.renderSpriteCubeFaces(
                matrices,
                vertices,
                waterSprite,
                0.4F, 0.4F, 0.4F,
                BiomeColors.getWaterColor(world, pos),
                light, overlay, PosHelper.ALL
        );
        matrices.pop();
    }
}
