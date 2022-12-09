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
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity.world.getTime() % 6 == 0 && entity instanceof LivingEntity living && isApplicable(living)) {
            ParticleUtils.spawnParticles(entity.world.getDimension().ultrawarm() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.COMPOSTER, entity, 1);
        }
    }
}
