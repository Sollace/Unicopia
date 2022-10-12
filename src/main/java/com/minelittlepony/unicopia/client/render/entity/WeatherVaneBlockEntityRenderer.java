package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.block.WeatherVaneBlock;
import com.minelittlepony.unicopia.block.WeatherVaneBlock.WeatherVane;
import com.minelittlepony.unicopia.client.render.RenderLayers;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WeatherVaneBlockEntityRenderer implements BlockEntityRenderer<WeatherVaneBlock.WeatherVane> {
    private static final Identifier TEXTURE = Unicopia.id("textures/entity/weather_vane.png");

    private final ModelPart root;
    private final ModelPart pole;

    public WeatherVaneBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        root = getTexturedModelData().createModel();
        pole = root.getChild("pole");
    }

    private static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        root.addChild("base", ModelPartBuilder.create()
                .uv(30, 14).mirrored().cuboid(-9, -1, 7, 2, 1, 2, Dilation.NONE), ModelTransform.pivot(8, 0, -8));

        ModelPartData pole = root.addChild("pole", ModelPartBuilder.create(), ModelTransform.NONE);

        pole.addChild("ew_arrow", ModelPartBuilder.create()
                .uv(0, -16).cuboid(0, -12, -8, 0, 5, 16, Dilation.NONE), ModelTransform.rotation(0, 0.7854F, 0));
        pole.addChild("apple", ModelPartBuilder.create()
                .uv(0, 2).cuboid(0, -27, -7, 0, 15, 15, Dilation.NONE)
                .uv(0, -11).cuboid(0, -9, -8, 0, 5, 16, Dilation.NONE)
                .uv(32, 0).cuboid(-0.5F, -14, -0.5F, 1, 14, 1, Dilation.NONE), ModelTransform.NONE);


        return TexturedModelData.of(modelData, 64, 32);
    }


    @Override
    public void render(WeatherVane entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        matrices.push();
        matrices.scale(1, -1, -1);
        matrices.translate(0.5F, 0, -0.5F);

        pole.yaw = entity.getAngle(tickDelta);
        root.render(matrices, vertices.getBuffer(RenderLayers.getEntityCutoutNoCull(TEXTURE, true)), light, overlay);

        matrices.pop();
    }
}
