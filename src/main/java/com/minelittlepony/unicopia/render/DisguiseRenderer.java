package com.minelittlepony.unicopia.render;

import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.spell.SpellDisguise;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

public class DisguiseRenderer {

    private static final DisguiseRenderer instance = new DisguiseRenderer();

    public static DisguiseRenderer instance() {
        return instance;
    }

    public boolean renderDisguise(Entity entity, float usedPartialTick) {
        Minecraft mc = Minecraft.getMinecraft();

        RenderManager renderMan = mc.getRenderManager();

        if (entity.getEntityData().hasKey("disguise") && entity.getEntityData().getBoolean("disguise")) {

            renderMan.setRenderShadow(true);
            renderStaticDisguise(renderMan, entity);
            renderMan.setRenderShadow(false);

            return true;
        } else {
            renderMan.setRenderShadow(renderMan.isRenderShadow() || usedPartialTick != 1);
        }

        return false;
    }

    protected void renderDisguise(RenderManager renderMan, Entity entity, double x, double y, double z) {
        entity.getEntityData().setBoolean("disguise", false);
        renderMan.renderEntity(entity, x, y, z, 0, 1, false);
        entity.getEntityData().setBoolean("disguise", true);
    }

    protected void renderStaticDisguise(RenderManager renderMan, Entity entity) {
        Entity observer = Minecraft.getMinecraft().getRenderViewEntity();

        double x = entity.posX - observer.posX;
        double y = entity.posY - observer.posY;
        double z = entity.posZ - observer.posZ;

        renderDisguise(renderMan, entity, x, y, z);
    }

    public boolean renderDisguiseToGui(IPlayer player) {
        SpellDisguise effect = player.getEffect(SpellDisguise.class, false);

        if (effect == null || effect.getDead()) {
            return false;
        }

        Minecraft mc = Minecraft.getMinecraft();
        RenderManager renderMan = mc.getRenderManager();

        if (renderMan.isRenderShadow()) {
            return false;
        }

        Entity e = effect.getDisguise();

        // Check for a disguise and render it in our place.
        if (e != null) {
            effect.update(player);

            e.setAlwaysRenderNameTag(false);
            e.setInvisible(false);
            e.posY = player.getOwner().posY;

            renderDisguise(renderMan, e, 0, 0, 0);
        }

        return true;
    }
}
