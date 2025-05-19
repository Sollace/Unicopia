package com.minelittlepony.unicopia.client.render.entity;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.block.ItemJarBlock;
import com.minelittlepony.unicopia.block.ItemJarBlock.FluidJarContents;
import com.minelittlepony.unicopia.block.jar.ItemsJarContents;
import com.minelittlepony.unicopia.block.jar.FakeFluidJarContents;
import com.minelittlepony.unicopia.block.jar.EntityJarContents;
import com.minelittlepony.unicopia.client.render.model.CubeModel;
import com.minelittlepony.unicopia.util.FluidHelper;
import com.minelittlepony.unicopia.util.PosHelper;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
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
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ItemJarBlockEntityRenderer implements BlockEntityRenderer<ItemJarBlock.TileData> {
    private static final Direction[] GLASS_SIDES = Arrays.stream(PosHelper.ALL).filter(i -> i != Direction.UP).toArray(Direction[]::new);
    private final ItemRenderer itemRenderer;
    private final EntityRenderDispatcher dispatcher;

    public ItemJarBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        itemRenderer = ctx.getItemRenderer();
        dispatcher = ctx.getEntityRenderDispatcher();
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

        FluidJarContents fluid = data.getFluid();
        if (fluid != null) {
            renderFluid(data, fluid, tickDelta, matrices, vertices, light, overlay);
        }

        FakeFluidJarContents milk = data.getFakeFluid();
        if (milk != null) {
            renderFluid(data, Fluids.WATER.getDefaultState(), milk.color(), FluidConstants.BUCKET, tickDelta, matrices, vertices, light, overlay);
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
        for (ItemStack stack : items.stacks()) {
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
        Entity e = entity.entity().get();
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
    }

    private void renderFluid(ItemJarBlock.TileData data, FluidJarContents fluid, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        FluidState state = FluidHelper.getFullFluidState(fluid.fluid());
        int color = getFluidColor(data.getWorld(), data.getPos(), state);
        renderFluid(data, state, color, fluid.amount(), tickDelta, matrices, vertices, light, overlay);
    }

    private void renderFluid(ItemJarBlock.TileData data, FluidState state, int color, long amount, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        Sprite[] sprite = getFluidSprite(data.getWorld(), data.getPos(), state);
        matrices.push();
        Sprite topSprite = sprite[0];
        float height = 0.6F * (amount / (float)FluidConstants.BUCKET);
        boolean opaque = Color.a(color) >= 1;
        CubeModel.render(
                matrices,
                vertices.getBuffer(opaque ? RenderLayer.getEntitySolid(topSprite.getAtlasId()) : RenderLayer.getEntityTranslucent(topSprite.getAtlasId())),
                topSprite.getMinU(), topSprite.getMinV(),
                topSprite.getMaxU(), topSprite.getMaxV(),
                0.28F, 0.01F, 0.28F,
                0.73F, 0.01F + height, 0.73F,
                color | 0xFF000000,
                light, overlay, Direction.UP
        );
        Sprite sideSprite = sprite[sprite.length - 1];
        CubeModel.render(
                matrices,
                vertices.getBuffer(opaque ? RenderLayer.getEntitySolid(sideSprite.getAtlasId()) : RenderLayer.getEntityTranslucent(sideSprite.getAtlasId())),
                sideSprite.getMinU(), sideSprite.getMinV(),
                sideSprite.getMaxU(), sideSprite.getMaxV(),
                0.28F, 0.01F, 0.28F,
                0.73F, 0.01F + height, 0.73F,
                color | 0xFF000000,
                light, overlay, GLASS_SIDES
        );
        matrices.pop();
    }

    private int getFluidColor(World world, BlockPos pos, FluidState state) {
        return getFluidHandler(state.getFluid()).getFluidColor(world, pos, state);
    }

    private Sprite[] getFluidSprite(@Nullable World world, BlockPos pos, FluidState state) {
        return getFluidHandler(state.getFluid()).getFluidSprites(world, pos, state);
    }

    private FluidRenderHandler getFluidHandler(Fluid fluid) {
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        if (handler == null) {
            return FluidRenderHandlerRegistry.INSTANCE.get(Fluids.WATER);
        }
        return handler;
    }
}
