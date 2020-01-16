package com.minelittlepony.unicopia.magic.spells;

import com.minelittlepony.unicopia.UParticles;
import com.minelittlepony.unicopia.entity.SpellcastEntity;
import com.minelittlepony.unicopia.magic.Affinity;
import com.minelittlepony.unicopia.magic.ICaster;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Line;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class SpellCharge extends AbstractAttachableSpell {

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
        return e.hasEffect();
    }

    @Override
    protected Box getSearchArea(ICaster<?> source) {
        return searchArea.offset(source.getOriginVector());
    }

    @Override
    public void render(ICaster<?> source) {
        if (source.getWorld().rand.nextInt(4 + source.getCurrentLevel() * 4) == 0) {
            SpellcastEntity target = getTarget(source);

            if (target != null) {
                Vec3d start = source.getEntity().getPositionVector();

                IShape line = new Line(start, target.getPositionVector());

                source.spawnParticles(line, (int)line.getVolumeOfSpawnableSpace(), pos -> {
                    ParticleTypeRegistry.getTnstance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos.add(start), 0, 0, 0, getTint());
                });
            }

        }
    }

    @Override
    public boolean update(ICaster<?> source) {
        super.update(source);

        if (!searching) {
            SpellcastEntity target = getTarget(source);

            if (target != null && !target.overLevelCap() && source.getCurrentLevel() > 0) {
                source.addLevels(-1);
                target.addLevels(1);
            }
        }

        return !getDead();
    }
}
