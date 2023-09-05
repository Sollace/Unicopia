package com.minelittlepony.unicopia.client.render.entity;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;

import com.minelittlepony.unicopia.entity.mob.SombraEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class SombraEntityModel extends EntityModel<SombraEntity> {

	private final ModelPart part;

	private final ModelPart head;
	private final ModelPart upperJaw;
	private final ModelPart lowerJaw;

	private final ModelPart body;

	public SombraEntityModel(ModelPart root) {
		this.part = root;
		this.head = root.getChild("head");
		this.upperJaw = head.getChild("upper_jaw");
		this.lowerJaw = head.getChild("lower_jaw");
		this.body = root.getChild("body");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		ModelPartData neck = root.addChild("body", ModelPartBuilder.create(), ModelTransform.of(0, 20, 0, 0, 0, 0.0436F));
		neck.addChild("tail", ModelPartBuilder.create()
		        .uv(50, 12).cuboid(3.0958F, -3.8679F, -1, 3, 7, 3, Dilation.NONE), ModelTransform.of(4, 3, -1, 0, 0, -1.0036F));
		neck.addChild("neck", ModelPartBuilder.create()
		        .uv(32, 10).cuboid(2.0438F, -5.9668F, -3, 4, 7, 5, Dilation.NONE), ModelTransform.of(0, 0, 0, 0, 0, -0.6981F));

		ModelPartData head = root.addChild("head", ModelPartBuilder.create()
		        .uv(0, 0).cuboid(-5, -13, -4, 8, 8, 8, Dilation.NONE) // head
        		.uv(0, 4).cuboid(1, -15, -4, 2, 2, 2, Dilation.NONE)  // ear
        		.uv(0, 4).cuboid(1, -15, 2, 2, 2, 2, Dilation.NONE)   // ear
        		, ModelTransform.pivot(0, 20, 0));

		head.addChild("mane", ModelPartBuilder.create()
		        .uv(32, 0).cuboid(-2.4982F, -6.1228F, -1.5F, 8, 7, 3, Dilation.NONE), ModelTransform.of(-1, -12, 0, 0, 0, 0.48F));

		head.addChild("lower_jaw", ModelPartBuilder.create()
		        .uv(32, 22).cuboid(-5, 0, -3, 7, 2, 6, Dilation.NONE), ModelTransform.of(-1, -3, 0, 0, 0, -0.5672F));
		head.addChild("upper_jaw", ModelPartBuilder.create()
		        .uv(32, 30).cuboid(-5, -2, -3, 6, 2, 6, Dilation.NONE), ModelTransform.of(-4, -5, 0, 0, 0, -0.1745F));
		head.addChild("hair", ModelPartBuilder.create()
		        .uv(0, 16).cuboid(-2, -8, -4, 8, 9, 8, new Dilation(0.25F)), ModelTransform.of(-3, -7.75F, 0, 0, 0, 0.0436F));

		ModelPartData crown = head.addChild("crown", ModelPartBuilder.create()
		        .uv(0, 16).cuboid(-2, -8, -4, 8, 3, 8, new Dilation(0.5F))
		        .uv(1, 1).cuboid(-3, -6, -0.75F, 1, 1, 1, Dilation.NONE), ModelTransform.of(-4, -8.75F, 0, 0, 0, 0.1309F));

		crown.addChild("spike_r2", ModelPartBuilder.create()
		        .uv(2, 20).cuboid(-1, -4, 0, 1, 4, 0, Dilation.NONE), ModelTransform.of(0, -5, 4.25F, -0.5087F, -0.1298F, -0.228F));
		crown.addChild("spike_l2", ModelPartBuilder.create()
		        .uv(2, 20).cuboid(-1, -4, 0, 1, 4, 0, Dilation.NONE), ModelTransform.of(3, -5, -4.25F, 0.5236F, 0, 0));
		crown.addChild("spike_l1", ModelPartBuilder.create()
		        .uv(2, 20).cuboid(-1, -4, 0, 1, 4, 0, Dilation.NONE), ModelTransform.of(0, -5, -4.25F, 0.5087F, -0.1298F, -0.228F));
		crown.addChild("spike_r1", ModelPartBuilder.create()
		        .uv(2, 20).cuboid(-1, -4, 0, 1, 4, 0, Dilation.NONE), ModelTransform.of(3, -5, 4.25F, -0.5236F, 0, 0));

		head.addChild("horn", ModelPartBuilder.create()
		        .uv(0, 0).cuboid(2.2139F, -6.8302F, -1, 1, 3, 1, new Dilation(0.1F)), ModelTransform.of(-3, -6, 0.5F, 0, 0, -0.829F))
	            .addChild("bone", ModelPartBuilder.create()
    		        .uv(4, 0).cuboid(2.5F, -6.3301F, -0.5F, 1, 2, 1, Dilation.NONE), ModelTransform.of(-1, -3, -0.5F, 0, 0, 0.1745F))
    		        .addChild("horn_tip", ModelPartBuilder.create()
		                .uv(4, 0).cuboid(1.5035F, -6.7686F, -0.5F, 1, 2, 1, new Dilation(-0.1F)), ModelTransform.of(0, -2, 0, 0, 0, 0.2182F));

		return TexturedModelData.of(data, 64, 64);
	}

	@Override
    public void animateModel(SombraEntity entity, float limbAngle, float limbDistance, float tickDelta) {
	    float jawsOpenAmount = entity.getBiteAmount(tickDelta);
        float scale = entity.getScaleFactor(tickDelta) * 1.7F;

        part.pivotY = scale * -20;
        part.xScale = scale;
        part.yScale = scale;
        part.zScale = scale;

	    lowerJaw.resetTransform();
        lowerJaw.pivotY -= jawsOpenAmount * 3;
        lowerJaw.pivotX -= jawsOpenAmount * 3;
        lowerJaw.roll += jawsOpenAmount - 0.9F;

        upperJaw.resetTransform();
        upperJaw.roll -= jawsOpenAmount * 0.2F;
    }

	@Override
	public void setAngles(SombraEntity entity, float limbAngle, float limbDistance, float animationProgress, float netHeadYaw, float headPitch) {
	    part.yaw = -MathHelper.HALF_PI;
	    part.pivotY += MathHelper.sin(animationProgress * 0.05F);
	    part.pivotZ = MathHelper.cos(animationProgress * 0.045F);

	    head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE;
	    head.yaw = netHeadYaw * MathHelper.RADIANS_PER_DEGREE;

	    body.roll = limbDistance * 0.3F;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		part.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}