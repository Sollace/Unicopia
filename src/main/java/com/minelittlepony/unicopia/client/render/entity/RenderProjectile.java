package com.minelittlepony.unicopia.client.render.entity;

import com.minelittlepony.unicopia.entity.item.AdvancedProjectileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class RenderProjectile extends RenderSnowball<AdvancedProjectileEntity> {

    public RenderProjectile(RenderManager renderManager) {
        super(renderManager, Items.POTIONITEM, MinecraftClient.getInstance().getRenderItem());
    }

    @Override
    public ItemStack getStackToRender(AdvancedProjectileEntity entity) {
        return entity.getItem();
    }
}
