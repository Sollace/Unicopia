package com.minelittlepony.unicopia.spell;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.particle.Particles;
import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Line;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SpellCharge extends AbstractSpell {

    private int desiredLevel = 0;

    boolean searching = true;

    private UUID targettedEntityId;
    private EntitySpell targettedEntity;

    private static final AxisAlignedBB searchArea = new AxisAlignedBB(-15, -15, -15, 15, 15, 15);

    @Override
    public String getName() {
        return "charge";
    }

    @Override
    public void render(ICaster<?> source, int level) {
        if (source.getWorld().rand.nextInt(4 + level * 4) == 0) {
            EntitySpell target = getTarget(source);

            if (target != null) {
                Vec3d start = source.getEntity().getPositionVector();
                Vec3d end = target.getPositionVector();

                IShape line = new Line(start, end);

                Random rand = source.getWorld().rand;

                for (int i = 0; i < line.getVolumeOfSpawnableSpace(); i++) {
                    Vec3d pos = line.computePoint(rand);
                    Particles.instance().spawnParticle(Unicopia.MAGIC_PARTICLE, false,
                            pos.x + start.x, pos.y + start.y, pos.z + start.z,
                            0, 0, 0);
                }
            }

        }
    }

    protected boolean canTargetEntity(Entity e) {
        return e instanceof EntitySpell && ((EntitySpell)e).hasEffect();
    }

    protected void setTarget(EntitySpell e) {
        searching = false;
        targettedEntity = e;
        targettedEntityId = e.getUniqueID();
    }

    protected EntitySpell getTarget(ICaster<?> source) {
        if (targettedEntity == null && targettedEntityId != null) {
            source.getWorld().getEntities(EntitySpell.class, e -> e.getUniqueID().equals(targettedEntityId)).stream().findFirst().ifPresent(this::setTarget);
        }

        if (targettedEntity != null && targettedEntity.isDead) {
            targettedEntity = null;
            targettedEntityId = null;
            searching = true;
        }

        return targettedEntity;
    }

    @Override
    public boolean update(ICaster<?> source, int level) {

        if (searching) {
            BlockPos origin = source.getOrigin();

            List<Entity> list = source.getWorld().getEntitiesInAABBexcluding(source.getEntity(),
                    searchArea.offset(origin), this::canTargetEntity).stream().sorted((a, b) -> {
                        return (int)(a.getDistanceSq(origin) - b.getDistanceSq(origin));
                    }).collect(Collectors.toList());

            if (list.size() > 0) {
                setTarget((EntitySpell)list.get(0));
            }
        } else {
            EntitySpell target = getTarget(source);

            if (target != null && !target.overLevelCap() && level > 0) {
                source.addLevels(-1);
                target.addLevels(1);
            }
        }

        return !isDead;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public int getCurrentLevel() {
        return desiredLevel;
    }

    @Override
    public void setCurrentLevel(int level) {
        desiredLevel = level;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        if (targettedEntityId != null) {
            compound.setUniqueId("target", targettedEntityId);
        }
        compound.setInteger("level", desiredLevel);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("target")) {
            targettedEntityId = compound.getUniqueId("target");
        }

        desiredLevel = compound.getInteger("level");
    }
}
