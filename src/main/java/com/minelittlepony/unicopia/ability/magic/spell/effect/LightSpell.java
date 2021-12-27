package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.ArrayList;
import java.util.List;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.FairyEntity;
import com.minelittlepony.unicopia.entity.UEntities;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class LightSpell extends AbstractSpell {
    public static final SpellTraits DEFAULT_TRAITS = new SpellTraits.Builder()
            .with(Trait.LIFE, 10)
            .with(Trait.AIR, 0.3F)
            .with(Trait.KINDNESS, 12)
            .with(Trait.ORDER, 25)
            .build();

    private int age;
    private int duration;

    private final List<EntityReference<FairyEntity>> lights = new ArrayList<>();

    protected LightSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
        duration = 120 + (int)(traits.get(Trait.FOCUS, 0, 160) * 19);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {

        age++;

        if (age % 20 == 0) {
            duration--;
        }

        if (duration <= 0) {
            return false;
        }

        if (!caster.isClient()) {
            if (lights.isEmpty()) {
                int size = 2 + caster.getWorld().random.nextInt(2) + (int)(getTraits().get(Trait.LIFE, 10, 20) - 10)/10;
                while (lights.size() < size) {
                    lights.add(new EntityReference<FairyEntity>());
                }
            }

            lights.forEach(ref -> {
                if (!ref.isPresent(caster.getWorld())) {
                    FairyEntity entity = UEntities.TWITTERMITE.create(caster.getWorld());
                    entity.setPosition(ref.getPosition().orElseGet(() -> {
                        return caster.getMaster().getPos().add(VecHelper.supply(() -> caster.getWorld().random.nextInt(3) - 1));
                    }));
                    entity.setMaster(caster.getMaster());
                    entity.world.spawnEntity(entity);

                    ref.set(entity);
                    setDirty();
                }
            });
        }

        return true;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.putInt("age", age);
        compound.putInt("duration", duration);
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
        age = compound.getInt("age");
        duration = compound.getInt("duration");
        lights.clear();
        if (compound.contains("lights", NbtElement.LIST_TYPE)) {
            compound.getList("lights", NbtElement.COMPOUND_TYPE).forEach(nbt -> {
                EntityReference<FairyEntity> light = new EntityReference<>();
                light.fromNBT((NbtCompound)nbt);
                lights.add(light);
            });
        }
    }
}
