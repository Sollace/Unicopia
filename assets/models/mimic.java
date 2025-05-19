// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports

package com.example.mod;
   
public class mimic extends EntityModel<Entity> {
	private final ModelPart lid;
	private final ModelPart tongue_r1;
	private final ModelPart upper_teeth;
	private final ModelPart cube_r1;
	private final ModelPart lower_teeth;
	private final ModelPart cube_r2;
	private final ModelPart right_leg;
	private final ModelPart left_leg;
	public mimic(ModelPart root) {
		this.lid = root.getChild("lid");
		this.lower_teeth = root.getChild("lower_teeth");
		this.right_leg = root.getChild("right_leg");
		this.left_leg = root.getChild("left_leg");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData lid = modelPartData.addChild("lid", ModelPartBuilder.create(), ModelTransform.of(0.0F, 17.0F, -7.0F, -0.829F, 0.0F, -3.1416F));

		ModelPartData tongue_r1 = lid.addChild("tongue_r1", ModelPartBuilder.create().uv(11, 34).cuboid(-3.0F, -10.0F, 1.0F, 6.0F, 1.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 7.0F, 7.0F, 0.5236F, 0.0F, 0.0F));

		ModelPartData upper_teeth = lid.addChild("upper_teeth", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -8.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-4.0F, -8.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(2.0F, -8.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 7.0F, 7.0F));

		ModelPartData cube_r1 = upper_teeth.addChild("cube_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -1.0F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-9.0F, -1.0F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-12.0F, -1.0F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-6.0F, -1.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-9.0F, -1.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-12.0F, -1.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -7.0F, -7.0F, 0.0F, 1.5708F, 0.0F));

		ModelPartData lower_teeth = modelPartData.addChild("lower_teeth", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -1.0F, 12.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-4.0F, -1.0F, 12.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(2.0F, -1.0F, 12.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 16.0F, -7.0F));

		ModelPartData cube_r2 = lower_teeth.addChild("cube_r2", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -1.0F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-9.0F, -1.0F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-12.0F, -1.0F, -6.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-6.0F, -1.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-9.0F, -1.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-12.0F, -1.0F, 5.0F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		ModelPartData right_leg = modelPartData.addChild("right_leg", ModelPartBuilder.create().uv(7, 30).cuboid(-2.5F, -1.5F, -3.0F, 5.0F, 7.0F, 6.0F, new Dilation(0.0F)), ModelTransform.pivot(3.5F, 25.5F, 1.0F));

		ModelPartData left_leg = modelPartData.addChild("left_leg", ModelPartBuilder.create().uv(7, 30).mirrored().cuboid(-9.5F, -1.5F, -3.0F, 5.0F, 7.0F, 6.0F, new Dilation(0.0F)).mirrored(false), ModelTransform.pivot(3.5F, 25.5F, 1.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		lid.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		lower_teeth.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		right_leg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		left_leg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}