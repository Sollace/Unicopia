package com.minelittlepony.unicopia.redux.magic.spells;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.core.magic.AbstractSpell;
import com.minelittlepony.unicopia.core.magic.Affinity;
import com.minelittlepony.unicopia.core.magic.ICaster;
import com.minelittlepony.unicopia.core.magic.IMagicEffect;
import com.minelittlepony.unicopia.core.magic.spell.SpellRegistry;
import com.minelittlepony.unicopia.redux.entity.SpellcastEntity;
import com.minelittlepony.unicopia.redux.entity.ai.FollowCasterGoal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Box;

public class FaithfulAssistantSpell extends AbstractSpell {

    private static final Box EFFECT_BOUNDS = new Box(-2, -2, -2, 2, 2, 2);

    @Nullable
    private IMagicEffect piggyBackSpell;

    @Override
    public String getName() {
        return "drake";
    }

    @Override
    public Affinity getAffinity() {
        return Affinity.GOOD;
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

    @Override
    public boolean isDead() {
        return super.isDead();
    }

    @Override
    public boolean isDirty() {
        return super.isDirty() || (piggyBackSpell != null && piggyBackSpell.isDirty());
    }

    @Override
    public void onPlaced(ICaster<?> caster) {
        if (caster.getEntity() instanceof SpellcastEntity) {
            SpellcastEntity living = (SpellcastEntity)caster.getEntity();

            ((PathNavigateGround)living.getNavigator()).setCanSwim(false);
            living.tasks.addTask(1, new EntityAISwimming(living));
            living.tasks.addTask(2, new FollowCasterGoal<>(caster, 1, 4, 70));

            living.setPosition(living.x, living.y, living.z);
        }
    }

    @Override
    public boolean update(ICaster<?> source) {
        if (piggyBackSpell == null) {
            Box bb = EFFECT_BOUNDS.offset(source.getOriginVector());

            source.getWorld().getEntities(source.getEntity(), bb, e -> e instanceof SpellcastEntity).stream()
                .map(i -> (SpellcastEntity)i)
                .filter(i -> i.hasEffect() && !(i.getEffect() instanceof FaithfulAssistantSpell))
                .findFirst().ifPresent(i -> {
                    piggyBackSpell = i.getEffect().copy();
                    piggyBackSpell.onPlaced(source);
                    i.setEffect(null);
                    setDirty(true);
                });
        }

        if (piggyBackSpell != null) {
            piggyBackSpell.update(source);
        }

        return true;
    }

    @Override
    public void render(ICaster<?> source) {
        if (piggyBackSpell != null) {
            piggyBackSpell.render(source);
        }
    }

    @Override
    public void toNBT(CompoundTag compound) {
        super.toNBT(compound);

        if (piggyBackSpell != null) {
            compound.put("effect", SpellRegistry.instance().serializeEffectToNBT(piggyBackSpell));
        }
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        super.fromNBT(compound);

        if (compound.containsKey("effect")) {
            if (piggyBackSpell != null) {
                piggyBackSpell.fromNBT(compound.getCompound("effect"));
            } else {
                piggyBackSpell = SpellRegistry.instance().createEffectFromNBT(compound.getCompound("effect"));
            }
        }
    }
}
