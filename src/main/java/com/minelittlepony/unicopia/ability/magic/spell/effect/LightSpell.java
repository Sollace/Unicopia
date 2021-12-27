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

    private int duration;

    private List<EntityReference<FairyEntity>> lights;

    protected LightSpell(SpellType<?> type, SpellTraits traits) {
        super(type, traits);
        duration = (int)(traits.get(Trait.FOCUS, 0, 160) * 19);
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {

        if (duration-- <= 0) {
            return false;
        }

        if (lights == null) {
            int size = 2 + caster.getWorld().random.nextInt(2) + (int)(getTraits().get(Trait.LIFE, 10, 20) - 10)/10;
            lights = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                lights.set(i, new EntityReference<FairyEntity>());
            }
        }

        if (!caster.isClient()) {
            lights.forEach(ref -> {
                if (!ref.isPresent(caster.getWorld())) {
                    FairyEntity entity = UEntities.TWITTERMITE.create(caster.getWorld());
                    entity.setPosition(ref.getPosition().orElseGet(() -> {
                        return caster.getMaster().getPos().add(VecHelper.supply(() -> caster.getWorld().random.nextInt(2) - 1));
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
        if (lights != null) {
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
        if (compound.contains("lights", NbtElement.LIST_TYPE)) {
            lights = new ArrayList<>();
            compound.getList("lights", NbtElement.COMPOUND_TYPE).forEach(nbt -> {
                EntityReference<FairyEntity> light = new EntityReference<>();
                light.fromNBT((NbtCompound)nbt);
                lights.add(light);
            });
        } else {
            lights = null;
        }
    }
}
