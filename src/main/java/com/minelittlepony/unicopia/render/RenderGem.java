package com.minelittlepony.unicopia.render;

import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.model.ModelGem;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderGem extends RenderLiving<EntitySpell> {

	private static final ResourceLocation gem = new ResourceLocation("unicopia", "textures/entity/gem.png");

	public RenderGem(RenderManager rendermanagerIn) {
		super(rendermanagerIn, new ModelGem(), 0);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySpell entity) {
		return gem;
	}

	@Override
	public boolean shouldRender(EntitySpell livingEntity, ICamera camera, double camX, double camY, double camZ) {
	    return true;
	}

	@Override
	protected float getDeathMaxRotation(EntitySpell entity) {
		return 0;
    }

	@Override
	protected boolean canRenderName(EntitySpell targetEntity) {
        return super.canRenderName(targetEntity) && (targetEntity.getAlwaysRenderNameTagForRender()
                 || targetEntity.hasCustomName() && targetEntity == renderManager.pointedEntity);
    }
}
