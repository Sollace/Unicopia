// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17+ for Yarn
// Paste this class into your mod and generate all required imports
public class tentacle extends EntityModel<Entity> {
	private final ModelPart bone_a;
	private final ModelPart flower_4;
	private final ModelPart cube_r1;
	private final ModelPart cube_r2;
	private final ModelPart flower_8;
	private final ModelPart cube_r3;
	private final ModelPart cube_r4;
	private final ModelPart flower_3;
	private final ModelPart cube_r5;
	private final ModelPart cube_r6;
	private final ModelPart flower_7;
	private final ModelPart cube_r7;
	private final ModelPart cube_r8;
	private final ModelPart flower_2;
	private final ModelPart cube_r9;
	private final ModelPart cube_r10;
	private final ModelPart flower_6;
	private final ModelPart cube_r11;
	private final ModelPart cube_r12;
	private final ModelPart flower_1;
	private final ModelPart cube_r13;
	private final ModelPart cube_r14;
	private final ModelPart flower_5;
	private final ModelPart cube_r15;
	private final ModelPart cube_r16;
	private final ModelPart bone_b;
	private final ModelPart bone_c;
	private final ModelPart flower_9;
	private final ModelPart cube_r17;
	private final ModelPart cube_r18;
	private final ModelPart bone_d;
	private final ModelPart flower_10;
	private final ModelPart cube_r19;
	private final ModelPart cube_r20;
	private final ModelPart bone_e;
	private final ModelPart bone_f;
	private final ModelPart flower_11;
	private final ModelPart cube_r21;
	private final ModelPart cube_r22;
	private final ModelPart bone_g;
	private final ModelPart bone_h;
	private final ModelPart flower_12;
	private final ModelPart cube_r23;
	private final ModelPart cube_r24;
	private final ModelPart flower_13;
	private final ModelPart cube_r25;
	private final ModelPart cube_r26;
	private final ModelPart flower_14;
	private final ModelPart cube_r27;
	private final ModelPart cube_r28;
	public tentacle(ModelPart root) {
		this.bone_a = root.getChild("bone_a");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bone_a = modelPartData.addChild("bone_a", ModelPartBuilder.create().uv(0, 0).cuboid(-7.0F, -10.0F, -7.0F, 14.0F, 16.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 24.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		ModelPartData flower_4 = bone_a.addChild("flower_4", ModelPartBuilder.create(), ModelTransform.of(6.0F, 0.6703F, -6.7725F, 1.5929F, -0.909F, -1.1179F));

		ModelPartData cube_r1 = flower_4.addChild("cube_r1", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r2 = flower_4.addChild("cube_r2", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_8 = flower_4.addChild("flower_8", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.9259F, -0.8201F, -2.1974F));

		ModelPartData cube_r3 = flower_8.addChild("cube_r3", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r4 = flower_8.addChild("cube_r4", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_3 = bone_a.addChild("flower_3", ModelPartBuilder.create(), ModelTransform.of(6.0F, 0.6703F, 4.2275F, -2.4079F, 0.2344F, -2.5374F));

		ModelPartData cube_r5 = flower_3.addChild("cube_r5", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r6 = flower_3.addChild("cube_r6", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_7 = flower_3.addChild("flower_7", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, -2.4079F, 0.2344F, -2.5374F));

		ModelPartData cube_r7 = flower_7.addChild("cube_r7", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r8 = flower_7.addChild("cube_r8", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_2 = bone_a.addChild("flower_2", ModelPartBuilder.create(), ModelTransform.of(-5.0F, 0.6703F, 4.2275F, -1.2698F, 0.9678F, -1.7981F));

		ModelPartData cube_r9 = flower_2.addChild("cube_r9", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r10 = flower_2.addChild("cube_r10", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_6 = flower_2.addChild("flower_6", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, -1.2698F, 0.9678F, -1.7981F));

		ModelPartData cube_r11 = flower_6.addChild("cube_r11", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r12 = flower_6.addChild("cube_r12", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_1 = bone_a.addChild("flower_1", ModelPartBuilder.create(), ModelTransform.of(-3.0F, 0.6703F, -7.7725F, 0.6103F, -0.0535F, -0.5864F));

		ModelPartData cube_r13 = flower_1.addChild("cube_r13", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r14 = flower_1.addChild("cube_r14", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_5 = flower_1.addChild("flower_5", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.6103F, -0.0535F, -0.5864F));

		ModelPartData cube_r15 = flower_5.addChild("cube_r15", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r16 = flower_5.addChild("cube_r16", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData bone_b = bone_a.addChild("bone_b", ModelPartBuilder.create().uv(0, 30).cuboid(-6.0F, -18.0F, -6.0F, 12.0F, 19.0F, 12.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -9.0F, 0.0F, -0.2618F, 0.0F, 0.0F));

		ModelPartData bone_c = bone_b.addChild("bone_c", ModelPartBuilder.create().uv(48, 20).cuboid(-5.0F, -23.0F, -5.0F, 10.0F, 23.0F, 10.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -16.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		ModelPartData flower_9 = bone_c.addChild("flower_9", ModelPartBuilder.create(), ModelTransform.of(4.0F, -15.6242F, -3.3435F, 2.9259F, -0.8201F, -2.4592F));

		ModelPartData cube_r17 = flower_9.addChild("cube_r17", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r18 = flower_9.addChild("cube_r18", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData bone_d = bone_c.addChild("bone_d", ModelPartBuilder.create().uv(40, 53).cuboid(-4.0F, -23.0F, -4.0F, 8.0F, 21.0F, 8.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -18.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		ModelPartData flower_10 = bone_d.addChild("flower_10", ModelPartBuilder.create(), ModelTransform.of(-2.0F, -17.1355F, 3.0055F, -2.8606F, -0.5942F, 2.4482F));

		ModelPartData cube_r19 = flower_10.addChild("cube_r19", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r20 = flower_10.addChild("cube_r20", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData bone_e = bone_d.addChild("bone_e", ModelPartBuilder.create().uv(0, 61).cuboid(-3.0F, -25.0F, -3.0F, 6.0F, 22.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -18.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		ModelPartData bone_f = bone_e.addChild("bone_f", ModelPartBuilder.create().uv(72, 53).cuboid(-3.0F, -17.0F, -3.0F, 6.0F, 15.0F, 6.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, -22.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		ModelPartData flower_11 = bone_f.addChild("flower_11", ModelPartBuilder.create(), ModelTransform.of(-2.0F, -6.0251F, 0.478F, 2.7587F, 0.3479F, 2.5432F));

		ModelPartData cube_r21 = flower_11.addChild("cube_r21", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r22 = flower_11.addChild("cube_r22", ModelPartBuilder.create().uv(86, 0).cuboid(-14.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData bone_g = bone_f.addChild("bone_g", ModelPartBuilder.create().uv(56, 0).cuboid(-2.6F, -16.0F, -2.6F, 5.2F, 15.0F, 5.2F, new Dilation(0.0F)), ModelTransform.of(0.0F, -15.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

		ModelPartData bone_h = bone_g.addChild("bone_h", ModelPartBuilder.create().uv(24, 61).cuboid(-2.1F, -17.0F, -2.1F, 4.2F, 15.0F, 4.2F, new Dilation(0.0F)), ModelTransform.of(0.0F, -13.0F, 0.0F, 0.2618F, 0.0F, 0.0F));

		ModelPartData flower_12 = bone_h.addChild("flower_12", ModelPartBuilder.create(), ModelTransform.of(0.0F, -15.1462F, 0.6365F, 2.7587F, 0.3479F, 2.9795F));

		ModelPartData cube_r23 = flower_12.addChild("cube_r23", ModelPartBuilder.create().uv(86, 0).cuboid(-50.2096F, 2.6718F, -13.6647F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r24 = flower_12.addChild("cube_r24", ModelPartBuilder.create().uv(86, 0).cuboid(-27.1617F, -35.2665F, 9.3832F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_13 = flower_12.addChild("flower_13", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, 2.7587F, 0.3479F, 2.5868F));

		ModelPartData cube_r25 = flower_13.addChild("cube_r25", ModelPartBuilder.create().uv(86, 0).cuboid(-50.2096F, 2.6718F, -13.6647F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r26 = flower_13.addChild("cube_r26", ModelPartBuilder.create().uv(86, 0).cuboid(-27.1617F, -35.2665F, 9.3832F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 3.1416F, 0.7854F, 1.5708F));

		ModelPartData flower_14 = flower_12.addChild("flower_14", ModelPartBuilder.create(), ModelTransform.of(0.0F, 0.0F, 0.0F, -1.9412F, -1.0444F, 1.7406F));

		ModelPartData cube_r27 = flower_14.addChild("cube_r27", ModelPartBuilder.create().uv(86, 0).cuboid(-50.2096F, 2.6718F, -13.6647F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 1.5708F, 0.0F, 0.7854F));

		ModelPartData cube_r28 = flower_14.addChild("cube_r28", ModelPartBuilder.create().uv(86, 0).cuboid(-27.1617F, -35.2665F, 9.3832F, 14.0F, 0.0F, 14.0F, new Dilation(0.0F)), ModelTransform.of(36.0432F, 15.5243F, -3.1436F, 3.1416F, 0.7854F, 1.5708F));
		return TexturedModelData.of(modelData, 128, 128);
	}
	@Override
	public void setAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		bone_a.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}