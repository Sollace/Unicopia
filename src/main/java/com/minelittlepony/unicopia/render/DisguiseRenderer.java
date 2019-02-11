package com.minelittlepony.unicopia.render;

import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.spell.IMagicEffect;
import com.minelittlepony.unicopia.spell.SpellDisguise;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.monster.EntityShulker;

public class DisguiseRenderer {

    private static final DisguiseRenderer instance = new DisguiseRenderer();

    public static DisguiseRenderer instance() {
        return instance;
    }

    public boolean renderDisguise(Entity entity, float usedPartialTick) {
        Minecraft mc = Minecraft.getMinecraft();

        RenderManager renderMan = mc.getRenderManager();

        if (entity.getEntityData().hasKey("disguise") && entity.getEntityData().getBoolean("disguise")) {
            entity.getEntityData().setBoolean("disguise", false);

            renderMan.setRenderShadow(true);

            if (isAttachedEntity(entity)) {
                double blockMoveX = entity.posX - Math.round(entity.posX - 0.5F);
                double blockMoveY = entity.posY - Math.round(entity.posY);
                double blockMoveZ = entity.posZ - Math.round(entity.posZ - 0.5F);

                renderMan.renderEntity(entity, -blockMoveX + 0.5, -blockMoveY, -blockMoveZ + 0.5, 0, 1, false);
            } else {
                renderMan.renderEntity(entity, 0, 0, 0, 0, 1, false);
            }
            renderMan.setRenderShadow(false);

            entity.getEntityData().setBoolean("disguise", true);

            return true;
        } else {
            renderMan.setRenderShadow(renderMan.isRenderShadow() || usedPartialTick != 1);
        }

        return false;
    }

    public boolean renderDisguiseToGui(IPlayer player) {
        IMagicEffect effect = player.getEffect(false);

        if (!(effect instanceof SpellDisguise) || effect.getDead()) {
            return false;
        }

        Minecraft mc = Minecraft.getMinecraft();
        RenderManager renderMan = mc.getRenderManager();

        if (renderMan.isRenderShadow()) {
            return false;
        }

        Entity e = ((SpellDisguise)effect).getDisguise();

        // Check for a disguise and render it in our place.
        if (e != null) {
            effect.update(player);
            e.setInvisible(false);
            e.setAlwaysRenderNameTag(false);

            renderMan.renderEntity(e, 0, 0, 0, 0, 1, false);
        }

        return true;
    }

    public boolean isAttachedEntity(Entity entity) {
        return entity instanceof EntityShulker
            || entity instanceof EntityFallingBlock;
    }
}
