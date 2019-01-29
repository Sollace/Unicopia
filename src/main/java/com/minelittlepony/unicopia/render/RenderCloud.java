package com.minelittlepony.unicopia.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.entity.EntityCloud;
import com.minelittlepony.unicopia.model.ModelCloud;

public class RenderCloud extends RenderLiving<EntityCloud> {
    private static final ResourceLocation cloud = new ResourceLocation("unicopia", "textures/entity/clouds.png");
    private static final ResourceLocation rainCloud = new ResourceLocation("unicopia", "textures/entity/clouds_storm.png");

    public RenderCloud(RenderManager rendermanagerIn) {
        super(rendermanagerIn, new ModelCloud(), 1f);
    }

    public float prepareScale(EntityCloud entity, float par2) {
    	float scale = entity.getCloudSize();

    	GL11.glScalef(scale, scale, scale);
    	return 0.0625F;
    }

    protected void renderModel(EntityCloud entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

        if (!entity.isDead) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, -entity.height/entity.getCloudSize() + 0.3F, 0);

	    	GL11.glEnable(GL11.GL_BLEND);

	    	Vec3d cloudColour = entity.world.getCloudColour(Minecraft.getMinecraft().getRenderPartialTicks());

    		GL11.glColor4f((float)cloudColour.x, (float)cloudColour.y, (float)cloudColour.z, entity.getOpaque() ? 1 : 0.8F);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

	    	super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

	        GL11.glDisable(GL11.GL_BLEND);
	        GL11.glColor4f(1, 1, 1, 1);

	        GlStateManager.popMatrix();
        }
    }

    protected ResourceLocation getEntityTexture(EntityCloud entity) {
    	if (entity.getIsRaining() && entity.getIsThundering()) {
    		return rainCloud;
    	}
        return cloud;
    }

    protected int getColorMultiplier(EntityCloud par1EntityLivingBase, float yaw, float pitch) {
        return 25;
    }

    protected float getDeathMaxRotation(EntityCloud par1EntityLivingBase) {
        return 0;
    }
}
