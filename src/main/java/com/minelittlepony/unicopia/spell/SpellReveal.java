package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

public class SpellReveal extends AbstractSpell {

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
            Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos, 0, 0, 0);
        });

        source.spawnParticles(UParticles.UNICORN_MAGIC, 5);
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.GOOD;
    }

}