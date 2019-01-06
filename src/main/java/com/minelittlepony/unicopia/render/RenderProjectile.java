package com.minelittlepony.unicopia.render;

import com.minelittlepony.unicopia.entity.EntityProjectile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RenderProjectile extends RenderSnowball<EntityProjectile> {

    public RenderProjectile(RenderManager renderManager) {
        super(renderManager, Items.POTIONITEM, Minecraft.getMinecraft().getRenderItem());
    }

    @Override
    public ItemStack getStackToRender(EntityProjectile entity) {
        return entity.getItem();
    }
}
