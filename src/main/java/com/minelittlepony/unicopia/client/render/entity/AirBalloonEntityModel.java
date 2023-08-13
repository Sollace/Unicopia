package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;

import com.minelittlepony.unicopia.entity.AirBalloonEntity;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;

public class AirBalloonEntityModel extends EntityModel<AirBalloonEntity> {

    private final ModelPart root;

    private float inflation;

    private boolean isBurner;
    private boolean isBalloon;

    private final List<ModelPart> ropes;

	public AirBalloonEntityModel(ModelPart root) {
	    this.root = root;
	    isBurner = root.hasChild("burner");
	    isBalloon = root.hasChild("canopy");

	    if (isBurner || isBalloon) {
	        ModelPart part = root.getChild(isBalloon ? "canopy" : "burner");
	        ropes = List.of(
                    part.getChild("rope_a"),
                    part.getChild("rope_b"),
                    part.getChild("rope_c"),
                    part.getChild("rope_d")
            );
	    } else {
	        ropes = List.of();
	    }
	}

	public static TexturedModelData getBasketModelData() {
		ModelData modelData = new ModelData();
		ModelPartData root = modelData.getRoot();
		ModelPartData basket = root.addChild("basket", ModelPartBuilder.create().uv(0, 0).cuboid(-16, -1, -16, 32, 2, 30, Dilation.NONE), ModelTransform.pivot(0, 24, 0));
		basket.addChild("walls", ModelPartBuilder.create().uv(0, 66).cuboid(-17, -12, -16, 2, 11, 30, Dilation.NONE)
        		.uv(64, 68).cuboid(15, -12, -16, 2, 11, 30, Dilation.NONE)
        		.uv(80, 38).cuboid(-16, -12, -17, 32, 11, 2, Dilation.NONE)
        		.uv(0, 32).cuboid(8, -12, 13, 8, 11, 2, Dilation.NONE)
        		.uv(0, 6).cuboid(-16, -12, 13, 8, 11, 2, Dilation.NONE), ModelTransform.NONE);
		basket.addChild("rim", ModelPartBuilder.create().uv(40, 34).cuboid(-18, -13, -17, 4, 2, 32, Dilation.NONE)
        		.uv(0, 32).cuboid(14, -13, -17, 4, 2, 32, Dilation.NONE)
        		.uv(80, 32).cuboid(-17, -13, -18, 34, 2, 4, Dilation.NONE)
        		.uv(0, 19).cuboid(7, -13, 12, 10, 2, 4, Dilation.NONE)
        		.uv(0, 0).cuboid(-17, -13, 12, 10, 2, 4, Dilation.NONE), ModelTransform.NONE);
		return TexturedModelData.of(modelData, 256, 128);
	}

	public static TexturedModelData getBurnerModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        ModelPartData burner = root.addChild("burner", ModelPartBuilder.create().uv(8, 0).cuboid(-6, -47, -6, 11, 15, 11, Dilation.NONE), ModelTransform.pivot(0, 24, 0));
        burner.addChild("rope_d", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(-5, -46, -6,  0.7854F, 0, -0.7854F));
        burner.addChild("rope_c", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(-4, -44,  3, -0.7854F, 0, -0.7854F));
        burner.addChild("rope_b", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of( 5, -46,  1, -0.7854F, 0,  0.7854F));
        burner.addChild("rope_a", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of( 5, -45, -6,  0.7854F, 0,  0.7854F));
        return TexturedModelData.of(modelData, 64, 128);
    }

	public static TexturedModelData getCanopyModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        ModelPartData balloon = root.addChild("canopy", ModelPartBuilder.create().cuboid(-54, -178, -59, 112, 120, 112, Dilation.NONE), ModelTransform.pivot(0, 24, 0));
        balloon.addChild("rope_d", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(-14, -11, -16, 0.4363F, 0, -0.4363F));
        balloon.addChild("rope_c", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(-14, -11, 11, -0.4363F, 0, -0.4363F));
        balloon.addChild("rope_b", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(17, -11, 11, -0.4363F, 0, 0.4363F));
        balloon.addChild("rope_a", ModelPartBuilder.create().cuboid(-2, -68, 0, 2, 68, 2, Dilation.NONE), ModelTransform.of(17, -11, -16, 0.4363F, 0, 0.4363F));
        return TexturedModelData.of(modelData, 512, 256);
    }

	@Override
	public void setAngles(AirBalloonEntity entity, float tickDelta, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	    inflation = entity.getInflation(tickDelta);
	    root.roll = MathHelper.sin((float)(entity.getX() - entity.prevX));
        root.pitch = MathHelper.sin((float)(entity.getZ() - entity.prevZ));

	    if (isBurner) {
	        boolean lifted = inflation > 0.8F;
	        root.pivotY = 32 * (1 - inflation);
	        root.pivotX = inflation * MathHelper.sin(limbSwingAmount + entity.age / 5F) / 4F;
	        ropes.forEach(rope -> rope.visible = lifted);
	    }

	    if (isBalloon) {
	        root.pivotY = 0;
	        root.pivotX = inflation * MathHelper.cos(limbSwingAmount + entity.age / 5F) / 4F;
	        if (entity.getBasketType() == BoatEntity.Type.BAMBOO) {
	            ropes.forEach(rope -> rope.pivotY = 0);
	        } else {
	            ropes.forEach(ModelPart::resetTransform);
	        }
	    }
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float r, float g, float b, float a) {
	    if (isBalloon) {
	        matrices.push();
	        matrices.translate(0, 1 * (1 - inflation), 0);
            matrices.scale(1, MathHelper.lerp(inflation, -0.05F, 1), 1);
            root.render(matrices, vertexConsumer, light, overlay, r, g, b, a);
            matrices.pop();
        } else {
            root.render(matrices, vertexConsumer, light, overlay, r, g, b, a);
        }
	}
}