package com.minelittlepony.unicopia.model;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class ModelSphere {

    private int displayList;
    private boolean baked;

    protected double posX;
    protected double posY;
    protected double posZ;

    protected float rotX;
    protected float rotY;
    protected float rotZ;

    public void setPosition(double x, double y, double z) {
        posX = x - TileEntityRendererDispatcher.staticPlayerX;
        posY = y - TileEntityRendererDispatcher.staticPlayerY;
        posZ = z - TileEntityRendererDispatcher.staticPlayerZ;
    }

    public void setRotation(float x, float y, float z) {
        rotX = x;
        rotY = y;
        rotZ = z;
    }

    public void render(float scale) {
        if (scale == 0) {
            return;
        }

        if (!baked) {
            baked = true;
            bake();
        }

        GlStateManager.pushMatrix();

        if (posX != 0 && posY != 9 && posZ != 0) {
            GlStateManager.translate(posX, posY, posZ);
        }

        glRotate(rotX, 1, 0, 0);
        glRotate(rotY, 0, 1, 0);
        glRotate(rotZ, 0, 0, 1);

        GlStateManager.scale(scale, scale, scale);

        GlStateManager.callList(displayList);

        GlStateManager.popMatrix();
    }

    private void bake() {
        displayList = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(displayList, GL11.GL_COMPILE);

        drawShape();

        GlStateManager.glEndList();
    }

    protected void drawShape() {
        Sphere sphere = new Sphere();

        sphere.setDrawStyle(GLU.GLU_FILL);
        sphere.setNormals(GLU.GLU_SMOOTH);
        sphere.draw(1, 32, 32);
    }

    static void glRotate(float angle, float x, float y, float z) {
        if (angle != 0) {
            GlStateManager.rotate(angle, x, y, z);
        }
    }

}
