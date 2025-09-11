package com.minelittlepony.unicopia.client.render.entity;

import java.util.List;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.util.math.MathHelper;

public class CrystalShardsEntityModel extends EntityModel<CrystalShardsEntityRenderer.State> {
	private final List<ModelPart> crystals;

	public CrystalShardsEntityModel(ModelPart root) {
		super(root);
		this.crystals = List.of(root.getChild("west"), root.getChild("north"), root.getChild("south"), root.getChild("east"), root.getChild("primary"));
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
	public void setAngles(CrystalShardsEntityRenderer.State state) {
	    super.setAngles(state);
	    float offset = 0;
	    float amplitude = 0.02F;

	    for (ModelPart part : crystals) {
	        part.resetTransform();

	        if (state.shaking) {
	            float animationTime = (state.age + ++offset) * 122F;
	            float sin = MathHelper.sin(animationTime) * amplitude;

	            part.pitch += sin;
	            part.yaw += MathHelper.cos(animationTime) * amplitude;
	            part.roll += -sin;
	        }
	    }
	}
}