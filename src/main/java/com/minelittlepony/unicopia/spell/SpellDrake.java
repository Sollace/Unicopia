package com.minelittlepony.unicopia.spell;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.entity.EntitySpell;
import com.minelittlepony.unicopia.entity.ai.EntityAIFollowCaster;

import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.AxisAlignedBB;

public class SpellDrake extends AbstractSpell {

    private static final AxisAlignedBB EFFECT_BOUNDS = new AxisAlignedBB(-2, -2, -2, 2, 2, 2);

    @Nullable
    private IMagicEffect piggyBackSpell;

    private boolean firstUpdate = true;

    @Override
    public String getName() {
        return "drake";
    }

    @Override
    public SpellAffinity getAffinity() {
        return SpellAffinity.NEUTRAL;
    }

    @Override
    public int getTint() {
        return 0xFAEBD7;
    }

    @Override
    public void setDead() {
        super.setDead();

        if (piggyBackSpell != null) {
            piggyBackSpell.setDead();
        }
    }

    @Override
    public boolean allowAI() {
        return true;
    }

    public boolean getDead() {
        return super.getDead() || (piggyBackSpell != null && piggyBackSpell.getDead());
    }

    @Override
    public boolean update(ICaster<?> source, int level) {

        if (firstUpdate) {
            firstUpdate = false;

            if (source.getOwner() instanceof EntitySpell) {
                EntitySpell living = (EntitySpell)source.getOwner();

                ((PathNavigateGround)living.getNavigator()).setCanSwim(false);
                living.tasks.addTask(1, new EntityAISwimming(living));
                living.tasks.addTask(2, new EntityAIFollowCaster(source, 1, 4, 6));
            }
        }

        if (piggyBackSpell == null) {
            AxisAlignedBB bb = EFFECT_BOUNDS.offset(source.getOriginVector());

            source.getWorld().getEntitiesInAABBexcluding(source.getEntity(), bb, e -> e instanceof EntitySpell).stream()
                .map(i -> (EntitySpell)i)
                .filter(i -> i.getEffect() != null && !(i.getEffect() instanceof SpellDrake))
                .findFirst().ifPresent(i -> {
                    piggyBackSpell = i.getEffect();
                    i.setEffect(null);
                });
        }

        return piggyBackSpell != null && piggyBackSpell.update(source, level);
    }

    @Override
    public void render(ICaster<?> source, int level) {
        if (piggyBackSpell != null) {
            piggyBackSpell.render(source, level);
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        if (piggyBackSpell != null) {
            compound.setTag("effect", SpellRegistry.instance().serializeEffectToNBT(piggyBackSpell));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("effect")) {
            piggyBackSpell = SpellRegistry.instance().createEffectFromNBT(compound.getCompoundTag("effect"));
        }
    }
}
