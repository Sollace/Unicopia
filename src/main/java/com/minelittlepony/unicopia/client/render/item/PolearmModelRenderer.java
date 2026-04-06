package com.minelittlepony.unicopia.client.render.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.special.SimpleSpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PolearmModelRenderer implements SimpleSpecialModelRenderer {
    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();

        int y = -9;

        ModelPartData pole = root.addChild("pole", ModelPartBuilder.create().uv(0, 6).cuboid(-0.5f, y, -0.5f, 1, 25, 1), ModelTransform.NONE);
        pole.addChild("base", ModelPartBuilder.create().uv(4, 0).cuboid(-1.5f, y - 2, -0.5f, 3, 2, 1), ModelTransform.NONE);
        pole.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-0.5f, y - 6, -0.5f, 1, 4, 1), ModelTransform.NONE);
        return TexturedModelData.of(data, 32, 32);
    }

    private final ModelPart model = getTexturedModelData().createModel();
    private final Identifier texture;

    public PolearmModelRenderer(Identifier texture) {
        this.texture = texture;
    }

    @Override
    public void render(ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay, boolean glint) {
        if (displayContext != ItemDisplayContext.GUI && displayContext != ItemDisplayContext.GROUND && displayContext != ItemDisplayContext.FIXED) {
            matrices.push();
            applyTransforms(displayContext, matrices);
            model.render(matrices, ItemRenderer.getItemGlintConsumer(vertices, RenderLayer.getEntitySolid(texture), false, glint), light, overlay, Colors.WHITE);
            matrices.pop();
        }
    }

    protected void applyTransforms(ItemDisplayContext displayContext, MatrixStack matrices) {
        if (displayContext.isFirstPerson()) {
            matrices.scale(1, -1, -1);
        } else {
            int swap = displayContext.isLeftHand() ? -1 : 1;
            matrices.scale(1.5F, -1.5F, -1.5F);
            float offsetX = swap * 0.05F;
            matrices.translate(offsetX, 0, 0.05F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-30 * swap), offsetX, 0.5F, offsetX);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30 * swap), offsetX, 0.5F, offsetX);
        }
    }

    public record Unbaked(Identifier texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels models) {
            return new PolearmModelRenderer(texture);
        }
    }
}
