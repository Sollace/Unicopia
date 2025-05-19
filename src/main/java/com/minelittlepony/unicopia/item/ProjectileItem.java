package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.projectile.Projectile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

abstract class ProjectileItem extends Item implements Projectile {

    private final float projectileDamage;

    public ProjectileItem(Item.Settings settings, float projectileDamage) {
        super(settings);
        this.projectileDamage = projectileDamage;
        Projectile.makeDispensable(this);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!player.shouldCancelInteraction()) {
            TypedActionResult<ItemStack> result = super.use(world, player, hand);
            if (result.getResult().isAccepted()) {
                return result;
            }
        }

        return triggerThrow(world, player, hand);
    }

    @Override
    public float getProjectileDamage(ItemStack stack) {
        return projectileDamage;
    }
}
