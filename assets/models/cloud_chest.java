// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class cloud_chest extends EntityModel<Entity> {
	private final ModelPart lid;
	private final ModelPart lock_r1;
	private final ModelPart bb_main;
	public cloud_chest(ModelPart root) {
		this.lid = root.getChild("lid");
		this.bb_main = root.getChild("bb_main");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData lid = modelPartData.addChild("lid", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, 13.8F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-1.0F, -1.0F, 14.0F, 2.0F, 2.0F, 1.0F, new Dilation(0.0F))
		.uv(0, 0).cuboid(-7.0F, -5.0F, 0.0F, 14.0F, 5.0F, 14.0F, new Dilation(0.3F)), ModelTransform.of(0.0F, 16.0F, -7.0F, 1.0908F, 0.0F, 0.0F));

		ModelPartData lock_r1 = lid.addChild("lock_r1", ModelPartBuilder.create().uv(0, 0).cuboid(-2.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F, new Dilation(0.0F)), ModelTransform.of(-2.0F, 1.0F, 14.3F, 0.0F, 0.0F, 1.5708F));

		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 19).cuboid(-7.0F, -10.0F, -7.0F, 14.0F, 10.0F, 14.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 64, 64);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		lid.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		bb_main.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}