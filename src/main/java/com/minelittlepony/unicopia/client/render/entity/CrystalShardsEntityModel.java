package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.entity.CrystalShardsEntity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class CrystalShardsEntityModel extends EntityModel<CrystalShardsEntity> {
	private final ModelPart part;

	public CrystalShardsEntityModel(ModelPart root) {
		this.part = root;
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		root.addChild("west", ModelPartBuilder.create().uv(12, 0).cuboid(-1, -10, -2, 3, 10, 3, Dilation.NONE), ModelTransform.rotation(0.4075F, 0.0378F, 1.0216F));
		root.addChild("north", ModelPartBuilder.create().uv(12, 0).cuboid(-1, -10, -2, 3, 10, 3, Dilation.NONE), ModelTransform.rotation(1.817F, 0.6399F, 2.0582F));
		root.addChild("south", ModelPartBuilder.create().uv(12, 0).cuboid(-2, -9, -2, 3, 10, 3, Dilation.NONE), ModelTransform.rotation(-1.8254F, 0.6819F, -1.3949F));
		root.addChild("east", ModelPartBuilder.create().uv(12, 13).cuboid(-2, -8, -2, 3, 8, 3, Dilation.NONE), ModelTransform.rotation(-0.1151F, 0.3935F, -0.7545F));
		root.addChild("primary", ModelPartBuilder.create().uv(0, 0).cuboid(-2, -20, -2, 3, 20, 3, Dilation.NONE), ModelTransform.rotation(0.2411F, 0.3339F, 0.1819F));
		return TexturedModelData.of(data, 32, 32);
	}

	@Override
	public void setAngles(CrystalShardsEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		part.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}