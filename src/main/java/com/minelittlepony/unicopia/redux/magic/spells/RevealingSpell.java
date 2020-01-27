package com.minelittlepony.unicopia.redux.magic.spells;

import com.minelittlepony.unicopia.core.UParticles;
import com.minelittlepony.unicopia.core.magic.AbstractSpell;
import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.core.magic.ISuppressable;
import com.minelittlepony.unicopia.core.util.shape.IShape;
import com.minelittlepony.unicopia.core.util.shape.Sphere;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;


public class RevealingSpell extends AbstractSpell {

    @Override
    public String getName() {
        return "reveal";
    }

    @Override
    public int getTint() {
        return 0x5CE81F;
    }

    @Override
    public void onPlaced(ICaster<?> source) {
        source.setCurrentLevel(1);
    }

    @Override
    public boolean update(ICaster<?> source) {
        source.findAllSpellsInRange(15).forEach(e -> {
            ISuppressable spell = e.getEffect(ISuppressable.class, false);

            if (spell != null && spell.isVulnerable(source, this)) {
                spell.onSuppressed(source);
                source.getWorld().playSound(null, e.getOrigin(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.2F, 0.5F);
            }
        });

        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        IShape area = new Sphere(false, 15);

        source.spawnParticles(area, 5, pos -> {
            ParticleTypeRegistry.getTnstance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos, 0, 0, 0, getTint());
        });

        source.spawnParticles(UParticles.UNICORN_MAGIC, 5, getTint());
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

}