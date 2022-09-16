package com.minelittlepony.unicopia.block.data;

import java.util.*;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityReference;

import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class Ether extends PersistentState {
    private static final Identifier ID = Unicopia.id("ether");

    public static Ether get(World world) {
        return WorldOverlay.getPersistableStorage(world, ID, Ether::new, Ether::new);
    }

    private final Map<Identifier, Set<EntityReference<?>>> advertisingEndpoints = new HashMap<>();

    private final Object locker = new Object();

    Ether(World world, NbtCompound compound) {
        this(world);
        compound.getKeys().forEach(key -> {
            Identifier typeId = Identifier.tryParse(key);
            if (typeId != null) {
                Set<EntityReference<?>> uuids = getIds(typeId);
                compound.getList(key, NbtElement.COMPOUND_TYPE).forEach(entry -> {
                    uuids.add(new EntityReference<>((NbtCompound)entry));
                });
            }
        });
    }

    Ether(World world) {

    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        synchronized (locker) {
            advertisingEndpoints.forEach((id, uuids) -> {
                NbtList list = new NbtList();
                uuids.forEach(uuid -> list.add(uuid.toNBT()));
                compound.put(id.toString(), list);
            });

            return compound;
        }
    }

    public void put(SpellType<?> spellType, Caster<?> caster) {
        synchronized (locker) {
            getIds(spellType.getId()).add(new EntityReference<>(caster.getEntity()));
        }
        markDirty();
    }

    public void remove(SpellType<?> spellType, UUID id) {
        synchronized (locker) {
            Identifier typeId = spellType.getId();
            Set<EntityReference<?>> refs = advertisingEndpoints.get(typeId);
            if (refs != null) {
                refs.removeIf(ref -> ref.getId().orElse(Util.NIL_UUID).equals(id));
                if (refs.isEmpty()) {
                    advertisingEndpoints.remove(typeId);
                }
                markDirty();
            }
        }
    }

    public Set<EntityReference<?>> getIds(SpellType<?> spellType) {
        return getIds(spellType.getId());
    }

    private Set<EntityReference<?>> getIds(Identifier typeId) {
        synchronized (locker) {
            return advertisingEndpoints.computeIfAbsent(typeId, i -> new HashSet<>());
        }
    }
}
