package com.minelittlepony.unicopia.server.world;

import java.util.*;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.CasterView;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class Ether extends PersistentState implements CasterView {
    private static final Identifier ID = Unicopia.id("ether");

    public static Ether get(World world) {
        return WorldOverlay.getPersistableStorage(world, ID, Ether::new, Ether::new);
    }

    private final Map<Identifier, Set<Entry>> advertisingEndpoints = new HashMap<>();

    private final Object locker = new Object();

    private final World world;

    Ether(World world, NbtCompound compound) {
        this(world);
        compound.getKeys().forEach(key -> {
            Identifier typeId = Identifier.tryParse(key);
            if (typeId != null) {
                Set<Entry> uuids = getEntries(typeId);
                compound.getList(key, NbtElement.COMPOUND_TYPE).forEach(entry -> {
                    Entry e = new Entry();
                    e.fromNBT((NbtCompound)entry);
                    uuids.add(e);
                });
            }
        });
    }

    Ether(World world) {
        this.world = world;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        synchronized (locker) {
            advertisingEndpoints.forEach((id, uuids) -> {
                NbtList list = new NbtList();
                uuids.forEach(uuid -> {
                    if (uuid.isAlive()) {
                        list.add(uuid.toNBT());
                    }
                });
                compound.put(id.toString(), list);
            });

            return compound;
        }
    }

    public Entry put(SpellType<?> spellType, Caster<?> caster) {
        synchronized (locker) {
            var entry = new Entry(caster);
            getEntries(spellType.getId()).add(entry);
            markDirty();
            return entry;
        }
    }

    public void remove(SpellType<?> spellType, UUID id) {
        synchronized (locker) {
            Identifier typeId = spellType.getId();
            Set<Entry> refs = advertisingEndpoints.get(typeId);
            if (refs != null) {
                refs.removeIf(ref -> ref.isDead() || ref.entity.getTarget().filter(target -> id.equals(target.uuid())).isPresent());
                if (refs.isEmpty()) {
                    advertisingEndpoints.remove(typeId);
                }
                markDirty();
            }
        }
    }

    public void remove(SpellType<?> spellType, Caster<?> caster) {
        remove(spellType, caster.asEntity().getUuid());
    }

    public Set<Entry> getEntries(SpellType<?> spellType) {
        return getEntries(spellType.getId());
    }

    private Set<Entry> getEntries(Identifier typeId) {
        synchronized (locker) {
            return advertisingEndpoints.compute(typeId, (k, old) -> {
                if (old == null) {
                    old = new HashSet<>();
                } else {
                    old.removeIf(Entry::isDead);
                }
                return old;
            });
        }
    }

    public Optional<Entry> getEntry(SpellType<?> spellType, Caster<?> caster) {
        synchronized (locker) {
            return getEntries(spellType).stream().filter(e -> e.entity.referenceEquals(caster.asEntity())).findFirst();
        }
    }

    public Optional<Entry> getEntry(SpellType<?> spellType, UUID uuid) {
        synchronized (locker) {
            return getEntries(spellType).stream().filter(e -> e.equals(uuid)).findFirst();
        }
    }

    @Override
    public World getWorld() {
        return world;
    }

    public class Entry implements NbtSerialisable {
        public final EntityReference<?> entity;
        private boolean removed;
        private boolean taken;

        public float pitch;
        public float yaw;

        public Entry() {
            entity = new EntityReference<>();
        }

        public Entry(Caster<?> caster) {
            entity = new EntityReference<>(caster.asEntity());
        }

        boolean isAlive() {
            return !removed;
        }

        boolean isDead() {
            return removed;
        }

        public void markDead() {
            Unicopia.LOGGER.debug("Marking " + entity.getTarget().orElse(null) + " as dead");
            removed = true;
            markDirty();
        }

        public boolean isAvailable() {
            return !removed && !taken;
        }

        public void setTaken(boolean taken) {
            this.taken = taken;
            markDirty();
        }

        @Override
        public void toNBT(NbtCompound compound) {
            entity.toNBT(compound);
            compound.putBoolean("removed", removed);
            compound.putBoolean("taken", taken);
            compound.putFloat("pitch", pitch);
            compound.putFloat("yaw", yaw);
        }

        @Override
        public void fromNBT(NbtCompound compound) {
            entity.fromNBT(compound);
            removed = compound.getBoolean("removed");
            taken = compound.getBoolean("taken");
            pitch = compound.getFloat("pitch");
            yaw = compound.getFloat("yaw");
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Entry e && e.entity.referenceEquals(entity);
        }

        public boolean equals(UUID uuid) {
            return entity.referenceEquals(uuid);
        }

        @Override
        public int hashCode() {
            return entity.hashCode();
        }
    }
}
