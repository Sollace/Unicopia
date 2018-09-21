package com.minelittlepony.unicopia.model;

import com.minelittlepony.render.model.ModelQuads;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.spell.SpellRegistry;
import com.minelittlepony.util.render.Color;
import com.minelittlepony.util.render.Vertex;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelGem extends ModelBase {

	private ModelRenderer body;

	public ModelGem() {
        textureWidth = 256;
        textureHeight = 256;

		body = new ModelRenderer(this);
		body.offsetY = 1.2f;

		int size = 1;

		body.cubeList.add(new ModelQuads(body).addFace(
			new Vertex( size, 0,     size, 0,     0.5f),
			new Vertex(-size, 0,     size, 0.25f, 0.25f),
			new Vertex( 0, size * 2, 0, 0,     0.25f),
			new Vertex( 0, size * 2, 0, 0,     0.25f)
		).addFace(
			new Vertex( size,  0,     size, 0,     0.25f),
			new Vertex(-size,  0,     size, 0.25f, 0),
			new Vertex( 0, -size * 2, 0, 0.25f, 0.25f),
			new Vertex( 0, -size * 2, 0, 0.25f, 0.25f)
		).addFace(
			new Vertex(size, 0,    -size, 0.25f, 0.5f),
			new Vertex(size, 0,     size, 0.5f,  0.25f),
			new Vertex(0, size * 2, 0, 0.25f, 0.25f),
			new Vertex(0, size * 2, 0, 0.25f, 0.25f)
		).addFace(
			new Vertex(size,  0,    -size, 0.25f, 0.25f),
			new Vertex(size,  0,     size, 0.5f,  0),
			new Vertex(0, -size * 2, 0, 0.5f,  0.25f),
			new Vertex(0, -size * 2, 0, 0.5f,  0.25f)
		).addFace(
			new Vertex(-size, 0,    -size, 0.5f,  0.5f),
			new Vertex( size, 0,    -size, 0.75f, 0.25f),
			new Vertex( 0, size * 2, 0, 0.5f,  0.25f),
			new Vertex( 0, size * 2, 0, 0.5f,  0.25f)
		).addFace(
			new Vertex(-size,  0,    -size, 0.5f,  0.25f),
			new Vertex( size,  0,    -size, 0.75f, 0),
			new Vertex( 0, -size * 2, 0, 0.75f, 0.25f),
			new Vertex( 0, -size * 2, 0, 0.75f, 0.25f)
		).addFace(
			new Vertex(-size, 0,     size, 0.75f, 0.5f),
			new Vertex(-size, 0,    -size, 1,     0.25f),
			new Vertex( 0, size * 2, 0, 0.75f, 0.25f),
			new Vertex( 0, size * 2, 0, 0.75f, 0.25f)
		).addFace(
			new Vertex(-size,  0,     size, 0.75f, 0.25f),
			new Vertex(-size,  0,    -size, 1,     0),
			new Vertex( 0, -size * 2, 0, 1,     0.25f),
			new Vertex( 0, -size * 2, 0, 1,     0.25f)
		));
	}

	@Override
	public void render(Entity entity, float time, float walkSpeed, float stutter, float yaw, float pitch, float scale) {

		GlStateManager.pushMatrix();

		EntitySpell spell = (EntitySpell)entity;

		float floatOffset = MathHelper.sin((spell.ticksExisted + stutter) / 10 + spell.hoverStart) / 10 + 0.1F;
        GlStateManager.translate(0, floatOffset, 0);

        floatOffset = (spell.ticksExisted + stutter) / 20;
        if (spell.getLevel() > 0) {
        	floatOffset *= spell.getLevel() + 1;
        }

        floatOffset += spell.hoverStart;
        floatOffset *= 180 / (float)Math.PI;

        GlStateManager.rotate(floatOffset, 0, 1, 0);

        body.render(scale);

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(1, 1);

        setLightingConditionsBrightness(0xF0F0);

        Color.glColor(SpellRegistry.instance().getSpellTint(spell.getEffect().getName()), 1);

        GlStateManager.scale(1.2F, 1.2F, 1.2F);
        GlStateManager.translate(0, -0.2F, 0);

		body.render(scale);

		setLightingConditionsBrightness(entity.getBrightnessForRender());

		GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

		GlStateManager.popMatrix();
	}

	private void setLightingConditionsBrightness(int brightness) {
        int texX = brightness % 0x10000;
        int texY = brightness / 0x10000;

	    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, texX, texY);
	}

	@Override
	public void setRotationAngles(float time, float walkSpeed, float stutter, float yaw, float pitch, float increment, Entity entity) {

    }
}
