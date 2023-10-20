package com.minelittlepony.unicopia.client.render.entity;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.cloud.CloudBedBlock;
import com.minelittlepony.unicopia.client.render.RenderLayers;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

public class CloudBedBlockEntityRenderer implements BlockEntityRenderer<CloudBedBlock.Tile> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/cloud_bed/white.png");

    private final ModelPart bedHead;
    private final ModelPart bedFoot;

    public CloudBedBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.bedHead = getHeadTexturedModelData().createModel();
        this.bedFoot = getFootTexturedModelData().createModel();
    }

    public static TexturedModelData getHeadTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(0, 3, 0, 16, 13, 6), ModelTransform.NONE);
        root.addChild("head_board", ModelPartBuilder.create()
                .uv(52, 24).cuboid(7, -4, -3, 2, 13, 3)
                .uv(0, 43).cuboid(-6, -7, -2, 13, 15, 3)
                .uv(52, 24).cuboid(-8, -4, -3, 2, 13, 3), ModelTransform.of(7.5F, 1, 0, MathHelper.HALF_PI, 0, 0));
        return TexturedModelData.of(data, 64, 64);
    }

    public static TexturedModelData getFootTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("main", ModelPartBuilder.create().uv(0, 22).cuboid(0, 0, 0, 16, 15, 6), ModelTransform.NONE);
        root.addChild("foot_board", ModelPartBuilder.create()
                .uv(52, 40).cuboid(6, -1, -2, 3, 10, 3)
                .uv(0, 43).cuboid(-6, -3, -3, 13, 11, 3)
                .uv(52, 40).cuboid(-8, -1, -2, 3, 10, 3), ModelTransform.of(7.5F, 15, 0, MathHelper.HALF_PI, 0, 0));
        return TexturedModelData.of(data, 64, 64);
    }

    @Override
    public void render(CloudBedBlock.Tile entity, float f, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        @Nullable
        World world = entity.getWorld();
        if (world == null) {
            renderModel(matrices, vertices, bedHead, Direction.SOUTH, TEXTURE, light, overlay, false);
            renderModel(matrices, vertices, bedFoot, Direction.SOUTH, TEXTURE, light, overlay, true);
            return;
        }

        BlockState state = entity.getCachedState();

        renderModel(matrices, vertices,
                state.get(BedBlock.PART) == BedPart.HEAD ? bedHead : bedFoot,
                state.get(BedBlock.FACING),
                TEXTURE,
                getModelLight(entity, light),
                overlay,
                false
        );
    }

    private int getModelLight(CloudBedBlock.Tile entity, int worldLight) {
        return DoubleBlockProperties.toPropertySource(
                BlockEntityType.BED,
                BedBlock::getBedPart,
                BedBlock::getOppositePartDirection,
                ChestBlock.FACING, entity.getCachedState(), entity.getWorld(),
                entity.getPos(),
                (w, pos) -> false
        ).apply(new LightmapCoordinatesRetriever<>()).get(worldLight);
    }

    private void renderModel(MatrixStack matrices, VertexConsumerProvider vertices, ModelPart part, Direction direction, Identifier texture, int light, int overlay, boolean translate) {
        matrices.push();
        matrices.translate(0, 0.5625f, translate ? -1 : 0);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180 + direction.asRotation()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        part.render(matrices, vertices.getBuffer(RenderLayers.getEntityTranslucent(texture)), light, overlay);
        matrices.pop();
    }
}
