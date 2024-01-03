// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class ignimious_bulb extends EntityModel<Entity> {
	private final ModelPart head;
	private final ModelPart cube_r1;
	private final ModelPart head_r1;
	private final ModelPart leaves;
	private final ModelPart cube_r2;
	private final ModelPart cube_r3;
	private final ModelPart cube_r4;
	private final ModelPart cube_r5;
	private final ModelPart cube_r6;
	private final ModelPart cube_r7;
	private final ModelPart cube_r8;
	private final ModelPart cube_r9;
	private final ModelPart cube_r10;
	private final ModelPart cube_r11;
	private final ModelPart cube_r12;
	private final ModelPart cube_r13;
	public ignimious_bulb(ModelPart root) {
		this.head = root.getChild("head");
		this.leaves = root.getChild("leaves");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-24.0432F, -0.9905F, -48.1305F, 48.0F, 23.0F, 48.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 7.0F, 24.0F, -0.1308F, 0.0057F, 0.0433F));

		ModelPartData cube_r1 = head.addChild("cube_r1", ModelPartBuilder.create().uv(112, 0).cuboid(-16.0F, -27.0F, 5.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(1.3731F, -21.6152F, -11.61F, -1.9802F, 0.1003F, 0.0006F));

		ModelPartData head_r1 = head.addChild("head_r1", ModelPartBuilder.create().uv(0, 71).cuboid(-23.0F, -16.0F, -46.0F, 46.0F, 23.0F, 46.0F, new Dilation(0.0F)), ModelTransform.of(-0.0432F, -0.9905F, -0.1305F, -0.1309F, 0.0F, 0.0F));

		ModelPartData leaves = modelPartData.addChild("leaves", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 27.0F, -1.0F));

		ModelPartData cube_r2 = leaves.addChild("cube_r2", ModelPartBuilder.create().uv(112, 0).cuboid(-16.0F, 3.0F, -55.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.8316F, -1.0127F, -3.0858F));

		ModelPartData cube_r3 = leaves.addChild("cube_r3", ModelPartBuilder.create().uv(112, 0).cuboid(-14.0F, 12.0F, -60.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.6063F, -0.075F, -3.1196F));

		ModelPartData cube_r4 = leaves.addChild("cube_r4", ModelPartBuilder.create().uv(112, 0).cuboid(-20.0F, 1.0F, -51.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.7079F, -1.4622F, 0.4842F));

		ModelPartData cube_r5 = leaves.addChild("cube_r5", ModelPartBuilder.create().uv(112, 0).cuboid(-16.0F, 6.0F, -58.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.4967F, 0.1003F, 0.0006F));

		ModelPartData cube_r6 = leaves.addChild("cube_r6", ModelPartBuilder.create().uv(-64, 140).cuboid(-30.0F, 7.0F, -83.0F, 64.0F, 0.0F, 64.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.8708F, -0.1388F, -3.0651F));

		ModelPartData cube_r7 = leaves.addChild("cube_r7", ModelPartBuilder.create().uv(-64, 140).cuboid(-30.0F, 7.0F, -83.0F, 64.0F, 0.0F, 64.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -1.084F, -1.4961F, 0.807F));

		ModelPartData cube_r8 = leaves.addChild("cube_r8", ModelPartBuilder.create().uv(-64, 140).cuboid(-30.0F, 3.0F, -83.0F, 64.0F, 0.0F, 64.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.2651F, -0.035F, -0.0332F));

		ModelPartData cube_r9 = leaves.addChild("cube_r9", ModelPartBuilder.create().uv(-64, 140).cuboid(-30.0F, 7.0F, -83.0F, 64.0F, 0.0F, 64.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -1.95F, 1.4932F, -1.6857F));

		ModelPartData cube_r10 = leaves.addChild("cube_r10", ModelPartBuilder.create().uv(112, 0).cuboid(-16.0F, 10.0F, -57.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -2.5197F, 1.466F, -2.0202F));

		ModelPartData cube_r11 = leaves.addChild("cube_r11", ModelPartBuilder.create().uv(112, 0).cuboid(-8.0F, 3.0F, -61.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.9628F, 0.6133F, -2.978F));

		ModelPartData cube_r12 = leaves.addChild("cube_r12", ModelPartBuilder.create().uv(112, 0).cuboid(-13.0F, 6.0F, -60.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.4768F, 0.8786F, -0.1186F));

		ModelPartData cube_r13 = leaves.addChild("cube_r13", ModelPartBuilder.create().uv(112, 0).cuboid(-16.0F, 0.0F, -59.0F, 32.0F, 0.0F, 32.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.2911F, -0.5857F, 0.0605F));
		return TexturedModelData.of(modelData, 256, 256);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		head.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		leaves.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}