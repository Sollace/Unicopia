package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.Affinity;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.player.PlayerCharmTracker;
import com.minelittlepony.unicopia.particle.ParticleUtils;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.*;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class PegasusAmuletItem extends AmuletItem implements PlayerCharmTracker.Charm {
    public PegasusAmuletItem(FabricItemSettings settings, int maxEnergy) {
        super(settings, maxEnergy);
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.NEUTRAL;
    }

    @Override
    public void onRemoved(Living<?> living, int timeWorn) {

    }

    @Override
    public void onAdded(Living<?> living) {

    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity.world.getTime() % 6 == 0 && entity instanceof LivingEntity living && isApplicable(living)) {
            ParticleUtils.spawnParticles(entity.world.getDimension().ultrawarm() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.COMPOSTER, entity, 1);
        }
    }
}
