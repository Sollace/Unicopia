// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class hanging_sandbag extends EntityModel<Entity> {
	private final ModelPart root;
	private final ModelPart bag;
	private final ModelPart cube_r1;
	private final ModelPart cube_r2;
	public hanging_sandbag(ModelPart root) {
		this.root = root.getChild("root");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData root = modelPartData.addChild("root", ModelPartBuilder.create().uv(16, 19).cuboid(-0.5F, 0.0F, -0.5F, 1.0F, 9.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

		ModelPartData bag = root.addChild("bag", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, 1.0F, -3.0F, 6.0F, 7.0F, 6.0F, new Dilation(0.0F))
		.uv(12, 14).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F, new Dilation(0.0F))
		.uv(0, 13).cuboid(-2.0F, 8.0F, -2.0F, 4.0F, 1.0F, 4.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 9.0F, 0.0F));

		ModelPartData cube_r1 = bag.addChild("cube_r1", ModelPartBuilder.create().uv(0, 14).cuboid(0.0F, 8.0F, -2.0F, 0.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		ModelPartData cube_r2 = bag.addChild("cube_r2", ModelPartBuilder.create().uv(0, 14).cuboid(0.0F, 8.0F, -2.0F, 0.0F, 4.0F, 4.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 1.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
		return TexturedModelData.of(modelData, 32, 32);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		root.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}