package com.minelittlepony.unicopia.magic.spell;

import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.Caster;
import com.minelittlepony.unicopia.magic.EtherialListener;
import com.minelittlepony.unicopia.magic.Spell;
import com.minelittlepony.unicopia.particles.MagicParticleEffect;
import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Line;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ChargingSpell extends AbstractLinkedSpell implements EtherialListener {

    private static final Box searchArea = new Box(-15, -15, -15, 15, 15, 15);

    @Override
    public String getName() {
        return "charge";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
    }

    @Override
    public int getTint() {
        return 0x7272B7;
    }

    @Override
    protected boolean canTargetEntity(SpellcastEntity e) {
        return e.hasSpell();
    }

    @Override
    protected Box getSearchArea(Caster<?> source) {
        return searchArea.offset(source.getOriginVector());
    }

    @Override
    public void render(Caster<?> source) {
        if (source.getWorld().random.nextInt(4 + source.getCurrentLevel() * 4) == 0) {
            SpellcastEntity target = getTarget(source);

            if (target != null) {
                Vec3d start = source.getEntity().getPos();

                Shape line = new Line(start, target.getPos());

                source.spawnParticles(line, (int)line.getVolumeOfSpawnableSpace(), pos -> {
                    source.addParticle(new MagicParticleEffect(getTint()), pos.add(start), Vec3d.ZERO);
                });
            }
        }
    }

    @Override
    public boolean update(Caster<?> source) {
        super.update(source);

        if (!searching) {
            SpellcastEntity target = getTarget(source);

            if (target != null && !target.overLevelCap() && source.getCurrentLevel() > 0) {
                source.addLevels(-1);
                target.addLevels(1);
            }
        }

        return !isDead();
    }

    @Override
    public void onPlaced(Caster<?> caster) {
        caster.notifyNearbySpells(this, 12, ADDED);
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        caster.notifyNearbySpells(this, 12, REMOVED);
    }

    @Override
    public void onNearbySpellChange(Caster<?> source, Spell effect, int newState) {
        if (effect instanceof AttractiveSpell && !isDead()) {
            setDead();
            setDirty(true);
            source.notifyNearbySpells(this, 12, newState);
        }
    }
}
