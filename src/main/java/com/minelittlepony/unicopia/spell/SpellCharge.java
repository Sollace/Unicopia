package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.init.UParticles;
import com.minelittlepony.unicopia.particle.Particles;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Line;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class SpellCharge extends AbstractAttachableSpell {

    private static final AxisAlignedBB searchArea = new AxisAlignedBB(-15, -15, -15, 15, 15, 15);

    @Override
    public String getName() {
        return "charge";
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.GOOD;
    }

    @Override
    public int getTint() {
        return 0x7272B7;
    }

    @Override
    protected boolean canTargetEntity(EntitySpell e) {
        return e.hasEffect();
    }

    @Override
    protected AxisAlignedBB getSearchArea(ICaster<?> source) {
        return searchArea.offset(source.getOriginVector());
    }

    @Override
    public void render(ICaster<?> source) {
        if (source.getWorld().rand.nextInt(4 + source.getCurrentLevel() * 4) == 0) {
            EntitySpell target = getTarget(source);

            if (target != null) {
                Vec3d start = source.getEntity().getPositionVector();

                IShape line = new Line(start, target.getPositionVector());

                source.spawnParticles(line, (int)line.getVolumeOfSpawnableSpace(), pos -> {
                    Particles.instance().spawnParticle(UParticles.UNICORN_MAGIC, false, pos.add(start), 0, 0, 0);
                });
            }

        }
    }

    @Override
    public boolean update(ICaster<?> source) {
        super.update(source);

        if (!searching) {
            EntitySpell target = getTarget(source);

            if (target != null && !target.overLevelCap() && source.getCurrentLevel() > 0) {
                source.addLevels(-1);
                target.addLevels(1);
            }
        }

        return !getDead();
    }
}
