package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.item.component.Charges;
import com.minelittlepony.unicopia.item.component.UDataComponentTypes;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.minecraft.entity.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class PegasusAmuletItem extends AmuletItem {
    public PegasusAmuletItem(Item.Settings settings, int maxEnergy) {
        super(settings.component(UDataComponentTypes.CHARGES, Charges.of(maxEnergy / 2, maxEnergy)));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity.getWorld().getTime() % 6 == 0 && entity instanceof LivingEntity living && isApplicable(living)) {
            ParticleUtils.spawnParticles(entity.getWorld().getDimension().ultrawarm() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.COMPOSTER, entity, 1);
        }
    }
}
