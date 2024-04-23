package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory.Context;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class CloudChestBlockEntityRenderer extends ChestBlockEntityRenderer<ChestBlockEntity> {
    private static final LightmapCoordinatesRetriever<ChestBlockEntity> LIGHTING = new LightmapCoordinatesRetriever<>();
    private final Model[] models;

    public CloudChestBlockEntityRenderer(Context ctx) {
        super(ctx);
        models = new Model[] {
                new Model(Model.getSingleChestModelData().createModel(), Unicopia.id("textures/entity/chest/cloud.png")),
                new Model(Model.getLeftChestModelData().createModel(), Unicopia.id("textures/entity/chest/cloud_left.png")),
                new Model(Model.getRightChestModelData().createModel(), Unicopia.id("textures/entity/chest/cloud_right.png"))
        };
    }

    @Override
    public void render(ChestBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getWorld() != null ? entity.getCachedState() : Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);

        if (!(state.getBlock() instanceof AbstractChestBlock)) {
            return;
        }

        Model model = models[state.getOrEmpty(ChestBlock.CHEST_TYPE).orElse(ChestType.SINGLE).ordinal()];
        var properties = getProperties(state, entity);

        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.get(ChestBlock.FACING).asRotation()));
        matrices.translate(-0.5f, -0.5f, -0.5f);
        model.setAngles(1 - (float)Math.pow(1 - properties.apply(ChestBlock.getAnimationProgressRetriever(entity)).get(tickDelta), 3));
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(model.texture)), properties.apply(LIGHTING).applyAsInt(light), overlay);
        matrices.pop();
    }

    public static DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> getProperties(BlockState state, ChestBlockEntity entity) {
        return entity.getWorld() != null
                ? ((AbstractChestBlock<?>)state.getBlock()).getBlockEntitySource(state, entity.getWorld(), entity.getPos(), true)
                : DoubleBlockProperties.PropertyRetriever::getFallback;
    }

    static class Model {
        private final ModelPart tree;
        private final ModelPart lid;

        private final Identifier texture;

        public Model(ModelPart tree, Identifier texture) {
            this.tree = tree;
            this.lid = tree.getChild("lid");
            this.texture = texture;
        }

        public static TexturedModelData getSingleChestModelData() {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();
            root.addChild("chest", ModelPartBuilder.create().uv(0, 19).cuboid(1, 0, 1, 14, 10, 14, Dilation.NONE), ModelTransform.NONE);
            root.addChild("lid", ModelPartBuilder.create()
                    .uv(0, 0).cuboid(6, -2, 13.8F, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(6, -1, 14, 2, 2, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(0, 0, 0, 14, 5, 14, new Dilation(0.3F)), ModelTransform.pivot(1, 9, 1))
                    .addChild("lock_r1", ModelPartBuilder.create()
                            .uv(0, 0).cuboid(-2, -4, -0.5F, 2, 4, 1, Dilation.NONE), ModelTransform.of(5, 1, 14.3F, 0, 0, 1.5708F));
            return TexturedModelData.of(data, 64, 64);
        }

        public static TexturedModelData getLeftChestModelData() {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();
            root.addChild("chest", ModelPartBuilder.create().uv(0, 19).cuboid(0, 0, 1, 15, 10, 14, Dilation.NONE), ModelTransform.NONE);
            root.addChild("lid", ModelPartBuilder.create()
                    .uv(0, 0).cuboid(6, -2, 13.8F, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(6, -1, 14, 2, 2, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(0, 0, 0, 15, 5, 14, new Dilation(0.3F)), ModelTransform.pivot(0, 9, 1))
                    .addChild("lock_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-2, -4, -0.5F, 2, 4, 1, Dilation.NONE), ModelTransform.of(5, 1, 14.3F, 0, 0, 1.5708F));
            return TexturedModelData.of(data, 64, 64);
        }

        public static TexturedModelData getRightChestModelData() {
            ModelData data = new ModelData();
            ModelPartData root = data.getRoot();
            root.addChild("chest", ModelPartBuilder.create().uv(0, 19).cuboid(1, 0, 1, 15, 10, 14, Dilation.NONE), ModelTransform.NONE);
            root.addChild("lid", ModelPartBuilder.create()
                    .uv(0, 0).cuboid(7, -2, 13.8F, 2, 4, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(7, -1, 14, 2, 2, 1, Dilation.NONE)
                    .uv(0, 0).cuboid(0, 0, 0, 15, 5, 14, new Dilation(0.3F)), ModelTransform.pivot(1, 9, 1))
                    .addChild("lock_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-2, -4, -0.5F, 2, 4, 1, Dilation.NONE), ModelTransform.of(6, 1, 14.3F, 0, 0, 1.5708F));
            return TexturedModelData.of(data, 64, 64);
        }

        public void setAngles(float animationProgress) {
            lid.pitch = -(animationProgress * 1.5707964f);
        }

        public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay) {
            tree.render(matrices, vertices, light, overlay);
        }
    }
}
