package com.minelittlepony.unicopia.magic.spell;

import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.SuppressableEffect;
import com.minelittlepony.unicopia.util.particles.UParticles;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;


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
    public void onPlaced(Caster<?> source) {
        source.setCurrentLevel(1);
    }

    @Override
    public boolean update(Caster<?> source) {
        source.findAllSpellsInRange(15).forEach(e -> {
            SuppressableEffect spell = e.getEffect(SuppressableEffect.class, false);

            if (spell != null && spell.isVulnerable(source, this)) {
                spell.onSuppressed(source);
                source.getWorld().playSound(null, e.getOrigin(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.2F, 0.5F);
            }
        });

        return true;
    }

    @Override
    public void render(Caster<?> source) {
        Shape area = new Sphere(false, 15);

        source.spawnParticles(area, 5, pos -> {
            source.addParticle(UParticles.UNICORN_MAGIC, pos, Vec3d.ZERO); // getTint()
        });

        source.spawnParticles(UParticles.UNICORN_MAGIC, 5); //getTint()
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

}