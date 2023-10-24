// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class cloud_bed<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "cloud_bed"), "main");
	private final ModelPart head;
	private final ModelPart foot;

	public cloud_bed(ModelPart root) {
		this.head = root.getChild("head");
		this.foot = root.getChild("foot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition main_r1 = head.addOrReplaceChild("main_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -21.0F, -9.0F, 16.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -16.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition head_board = head.addOrReplaceChild("head_board", CubeListBuilder.create().texOffs(52, 24).addBox(7.0F, -13.0F, -3.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 43).addBox(-6.0F, -15.0F, -2.0F, 13.0F, 15.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(52, 24).addBox(-8.0F, -13.0F, -3.0F, 2.0F, 13.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 7.0F));

		PartDefinition foot = partdefinition.addOrReplaceChild("foot", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition main_r2 = foot.addOrReplaceChild("main_r2", CubeListBuilder.create().texOffs(0, 22).addBox(-8.0F, -8.0F, -9.0F, 16.0F, 15.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition head_board2 = foot.addOrReplaceChild("head_board2", CubeListBuilder.create().texOffs(52, 40).addBox(6.0F, -13.0F, -2.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 43).addBox(-6.0F, -15.0F, -3.0F, 13.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(52, 40).addBox(-8.0F, -13.0F, -2.0F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 3.0F, -7.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		foot.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}