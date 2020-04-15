package com.minelittlepony.unicopia.client.render;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.magic.spell.DisguiseSpell;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;

public class DisguiseRenderer {

    private static final DisguiseRenderer INSTANCE = new DisguiseRenderer();

    public static DisguiseRenderer getInstance() {
        return INSTANCE;
    }

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private boolean rendering;

    public boolean renderDisguise(Entity entity, float usedPartialTick) {

        EntityRenderDispatcher renderMan = mc.getEntityRenderManager();

        if (rendering) {
            renderMan.setRenderShadows(true);
            renderStaticDisguise(renderMan, entity);
            renderMan.setRenderShadows(false);

            return true;
        } else {
            renderMan.setRenderShadows(renderMan.shouldRenderShadows() || usedPartialTick != 1);
        }

        return false;
    }

    protected void renderStaticDisguise(EntityRenderDispatcher renderMan, Entity entity) {
        Entity observer = mc.getCameraEntity();

        double x = entity.x - observer.x;
        double y = entity.y - observer.y;
        double z = entity.z - observer.z;

        renderDisguise(renderMan, entity, x, y, z);
    }

    public boolean renderDisguiseToGui(Pony player) {
        DisguiseSpell effect = player.getEffect(DisguiseSpell.class, false);

        if (effect == null || effect.isDead()) {
            return false;
        }

        EntityRenderDispatcher renderMan = mc.getEntityRenderManager();

        if (renderMan.shouldRenderShadows()) {
            return false;
        }

        Entity e = effect.getDisguise();

        // Check for a disguise and render it in our place.
        if (e != null) {
            effect.update(player);

            e.setCustomNameVisible(false);
            e.setInvisible(false);
            e.y = player.getOwner().y;

            renderDisguise(renderMan, e, 0, 0, 0);
        }

        return true;
    }

    protected void renderDisguise(EntityRenderDispatcher renderMan, Entity entity, double x, double y, double z) {
        rendering = false;
        renderMan.render(entity, x, y, z, 0, 1, false);
        rendering = true;
    }
}
