package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.SpellAttributes;
import com.minelittlepony.unicopia.ability.magic.spell.TimedSpell;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.EntityReference.EntityValues;
import com.minelittlepony.unicopia.entity.mob.FairyEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class LightSpell extends AbstractSpell implements TimedSpell, ProjectileDelegate.HitListener {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.LIFE, 10)
            .with(Trait.AIR, 0.3F)
            .with(Trait.KINDNESS, 12)
            .with(Trait.ORDER, 25)
            .build();

    public static void appendTooltip(CustomisedSpellType<? extends LightSpell> type, List<Text> tooltip) {
        TimedSpell.appendDurationTooltip(type, tooltip);
        tooltip.add(SpellAttributes.of(SpellAttributes.ORB_COUNT, 2 + (int)(type.relativeTraits().get(Trait.LIFE, 10, 20) / 10F)));
    }

    private final Timer timer;

    private final List<EntityReference<FairyEntity>> lights = new ArrayList<>();

    protected LightSpell(CustomisedSpellType<?> type) {
        super(type);
        timer = new Timer(BASE_DURATION + TimedSpell.getExtraDuration(getTraits()));
    }

    @Override
    public Timer getTimer() {
        return timer;
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {

        if (situation == Situation.PROJECTILE) {
            return false;
        }

        timer.tick();

        if (timer.getTicksRemaining() <= 0) {
            return false;
        }

        setDirty();

        if (!caster.isClient()) {
            if (lights.isEmpty()) {
                int size = 2 + caster.asWorld().random.nextInt(2) + (int)(getTraits().get(Trait.LIFE, 10, 20) - 10)/10;
                while (lights.size() < size) {
                    lights.add(new EntityReference<>());
                }
            }

            lights.forEach(ref -> {
                if (ref.getOrEmpty(caster.asWorld()).isEmpty()) {
                    FairyEntity entity = UEntities.TWITTERMITE.create(caster.asWorld());
                    entity.setPosition(ref.getTarget().map(EntityValues::pos).orElseGet(() -> {
                        return caster.getOriginVector().add(VecHelper.supply(() -> caster.asWorld().random.nextInt(3) - 1));
                    }));
                    entity.setMaster(caster);
                    entity.getWorld().spawnEntity(entity);

                    ref.set(entity);
                    setDirty();
                }
            });
        }

        return true;
    }

    @Override
    public void onImpact(MagicProjectileEntity projectile) {
        Caster.of(projectile.getMaster()).ifPresent(caster -> getTypeAndTraits().apply(caster, CastingMethod.INDIRECT));
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (caster.isClient()) {
            return;
        }
        lights.forEach(ref -> {
            ref.ifPresent(caster.asWorld(), e -> {
                e.getWorld().sendEntityStatus(e, (byte)60);
                e.discard();
            });
        });
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        timer.toNBT(compound);
        if (!lights.isEmpty()) {
            NbtList list = new NbtList();
            lights.forEach(light -> {
                list.add(light.toNBT());
            });
            compound.put("lights", list);
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        timer.fromNBT(compound);
        lights.clear();
        if (compound.contains("lights", NbtElement.LIST_TYPE)) {
            compound.getList("lights", NbtElement.COMPOUND_TYPE).forEach(nbt -> {
                lights.add(new EntityReference<>((NbtCompound)nbt));
            });
        }
    }
}
