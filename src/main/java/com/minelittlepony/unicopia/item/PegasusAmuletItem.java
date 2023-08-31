package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.entity.ItemTracker;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class PegasusAmuletItem extends AmuletItem implements ItemTracker.Trackable {
    public PegasusAmuletItem(FabricItemSettings settings, int maxEnergy) {
        super(settings, maxEnergy);
    }

    @Override
    public void onUnequipped(Living<?> living, long timeWorn) {

    }

    @Override
    public void onEquipped(Living<?> living) {

    }

    @Override
    public boolean isApplicable(ItemStack stack) {
        return super.isApplicable(stack);
    }

    @Override
    public int getDefaultCharge() {
        return getMaxCharge() / 2;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity.getWorld().getTime() % 6 == 0 && entity instanceof LivingEntity living && isApplicable(living)) {
            ParticleUtils.spawnParticles(entity.getWorld().getDimension().ultrawarm() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.COMPOSTER, entity, 1);
        }
    }
}
