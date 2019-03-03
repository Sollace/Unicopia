package com.minelittlepony.unicopia.model;

import com.minelittlepony.render.model.ModelQuads;
import com.minelittlepony.unicopia.entity.EntitySpell;
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
        GlStateManager.translate(0, floatOffset - entity.getEyeHeight(), 0);

        floatOffset = (spell.ticksExisted + stutter) / 20;
        if (spell.getCurrentLevel() > 0) {
            floatOffset *= spell.getCurrentLevel() + 1;
        }

        floatOffset += spell.hoverStart;
        floatOffset *= 180 / (float)Math.PI;

        GlStateManager.pushMatrix();

        if (spell.overLevelCap()) {
            GlStateManager.translate(Math.sin(stutter) / 5, 0, Math.cos(stutter) / 5);

            GlStateManager.rotate((float)Math.sin(stutter), 0, 1, 0);
        }

        GlStateManager.rotate(floatOffset, 0, 1, 0);

        body.render(scale);

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(1, 1);

        setLightingConditionsBrightness(0xF0F0);

        if (spell.hasEffect()) {
            Color.glColor(spell.getEffect().getTint(), 1);
        }

        int tiers = Math.min(spell.getCurrentLevel(), 5);

        for (int i = 0; i <= tiers; i++) {
            float grow = (1 + i) * 0.2F;

            GlStateManager.scale(1 + grow, 1 + grow, 1 + grow);
            GlStateManager.translate(0, -grow, 0);
            renderOverlay(grow, scale);

            if (i == 5) {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(-floatOffset * 0.9F, 0, 1, 0);
                GlStateManager.translate(0.6F, 0.8F, 0);
                GlStateManager.scale(0.4F, 0.4F, 0.4F);
                renderOverlay(grow, scale);
                GlStateManager.popMatrix();
            }

        }
        GlStateManager.popMatrix();

        for (int i = spell.getCurrentLevel(); i > 0; i--) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(floatOffset / i, 0, 1, 0);
            GlStateManager.translate(0.6F, 0, 0);
            renderOverlay(0.6F, scale);
            GlStateManager.popMatrix();
        }

        setLightingConditionsBrightness(entity.getBrightnessForRender());

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        GlStateManager.popMatrix();
    }

    protected void renderOverlay(float grow, float scale) {
        body.render(scale);
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
