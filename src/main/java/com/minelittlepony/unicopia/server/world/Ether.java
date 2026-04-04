package com.minelittlepony.unicopia.server.world;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.server.world.chunk.Chunk;
import com.minelittlepony.unicopia.server.world.chunk.PositionalDataMap;
import com.minelittlepony.unicopia.util.Untyped;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class Ether extends PersistentState {
    private static final Identifier ID = Unicopia.id("ether");
    private static final Codec<Map<UUID, Map<UUID, Entry>>> ENDPOINT_CODEC = Codec.unboundedMap(Uuids.CODEC, Codec.unboundedMap(Uuids.CODEC, Entry.CODEC));
    private static final RecordCodecBuilder<Ether, Map<Identifier, Map<UUID, Map<UUID, Entry>>>> DATA_MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, ENDPOINT_CODEC).fieldOf("endpoints").<Ether>forGetter(o -> Untyped.cast(o.endpoints));

    private static final PersistentStateKey<Ether> KEY = new PersistentStateKey<>(ID, context -> {
        return RecordCodecBuilder.create(i -> i.group(DATA_MAP_CODEC).apply(i, endpoints -> new Ether(context.getWorldOrThrow(), endpoints)));
    }, Ether::new);

    public static Ether get(WorldView world) {
        return KEY.get(world);
    }

    private final Map<Identifier, Map<UUID, Map<UUID, MutableEntry<?>>>> endpoints;
    private final PositionalDataMap<MutableEntry<?>> positionData = new PositionalDataMap<>();

    private final Object locker = new Object();

    private final World world;

    private Ether(World world, Map<Identifier, Map<UUID, Map<UUID, Entry>>> endpoints) {
        this.world = world;
        this.endpoints = endpoints.entrySet().stream().map(endpoint -> {
            Map<UUID, Map<UUID, MutableEntry<?>>> entities = endpoint.getValue().entrySet().stream().map(entry -> {
                    Map<UUID, MutableEntry<?>> spells = entry.getValue().entrySet().stream()
                            .filter(c -> !c.getValue().removed())
                            .collect(Collectors.toMap(Map.Entry::getKey, c -> new MutableEntry<>(c.getValue())));
                    return spells.isEmpty() ? null : Map.entry(entry.getKey(), spells);
                })
                .filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return entities.isEmpty() ? null : Map.entry(endpoint.getKey(), entities);
        }).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    Ether(World world) {
        this.world = world;
        this.endpoints = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Spell> MutableEntry<T> getOrCreate(T spell, Caster<?> caster) {
        synchronized (locker) {
            MutableEntry<T> entry = (MutableEntry<T>)endpoints
                    .computeIfAbsent(spell.getTypeAndTraits().type().getId(), typeId -> new HashMap<>())
                    .computeIfAbsent(caster.asEntity().getUuid(), entityId -> new HashMap<>())
                    .computeIfAbsent(spell.getUuid(), spellid -> {
                        markDirty();
                        return new MutableEntry<>(spell, caster);
                    });

            if (entry.spell.get() != spell) {
                entry.spell = new WeakReference<>(spell);
                markDirty();
            }
            if (entry.removed) {
                entry.removed = false;
                positionData.update(entry);
                markDirty();
            }
            return entry;
        }
    }

    public void tick(World world) {
        endpoints.values().forEach(byType -> {
            byType.values().forEach(entries -> {
                entries.values().forEach(MutableEntry::update);
            });
        });
    }

    public <T extends Spell> void remove(SpellType<T> spellType, UUID entityId) {
        synchronized (locker) {
            endpoints.computeIfPresent(spellType.getId(), (typeId, entries) -> {
                Map<UUID, MutableEntry<?>> data = entries.remove(entityId);
                if (data != null) {
                    markDirty();
                    data.values().forEach(positionData::remove);
                }
                return entries.isEmpty() ? null : entries;
            });
        }
    }

    public void remove(SpellType<?> spellType, Caster<?> caster) {
        remove(spellType, caster.asEntity().getUuid());
    }

    public <T extends Spell> void remove(T spell, Caster<?> caster) {
        MutableEntry<T> entry = get(spell, caster);
        if (entry != null) {
            entry.markDead();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Spell> MutableEntry<T> get(T spell, Caster<?> caster) {
        return get((SpellType<T>)spell.getTypeAndTraits().type(), caster.asEntity().getUuid(), spell.getUuid());
    }

    public <T extends Spell> MutableEntry<T> get(SpellType<T> spell, EntityReference.EntityValues<?> entityId, @Nullable UUID spellId) {
        return get(spell, entityId.uuid(), spellId);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Spell> MutableEntry<T> get(SpellType<T> spell, UUID entityId, @Nullable UUID spellId) {
        if (spellId == null) {
            return null;
        }
        synchronized (locker) {
            MutableEntry<?> entry = endpoints
                    .getOrDefault(spell.getId(), Map.of())
                    .getOrDefault(entityId, Map.of())
                    .get(spellId);
            return entry == null || entry.removed() ? null : (MutableEntry<T>)entry;
        }
    }

    public <T extends Spell> boolean anyMatch(SpellType<T> spellType, BiPredicate<T, Caster<?>> condition) {
        return anyMatch(spellType, entry -> {
            var spell = entry.getSpell();
            var caster = entry.getCaster();
            return spell != null && caster != null && condition.test(spell, caster);
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends Spell> boolean anyMatch(SpellType<T> spellType, Predicate<MutableEntry<T>> condition) {
        synchronized (locker) {
            for (var entries : endpoints.getOrDefault(spellType.getId(), Map.of()).values()) {
                for (var entry : entries.values()) {
                    if (!entry.removed() && condition.test((MutableEntry<T>)entry)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Set<MutableEntry<?>> getAtPosition(BlockPos pos) {
        return world.isClient() ? Set.of() : positionData.getState(pos);
    }

    public Chunk<MutableEntry<?>> getChunk(ChunkPos pos) {
        return world.isClient() ? null : positionData.getChunk(pos);
    }

    public interface Entry {
        Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                MapCodec.assumeMapUnsafe(EntityReference.CODEC).forGetter(Entry::entity),
                Codec.BOOL.fieldOf("removed").forGetter(Entry::removed),
                Codec.FLOAT.fieldOf("pitch").forGetter(Entry::pitch),
                Codec.FLOAT.fieldOf("yaw").forGetter(Entry::yaw),
                Codec.FLOAT.fieldOf("radius").forGetter(Entry::radius),
                Uuids.CODEC.optionalFieldOf("spellId", null).forGetter(Entry::spellId),
                CodecUtils.setOf(Uuids.CODEC).fieldOf("claimants").forGetter(Entry::claimants)
        ).apply(i, FrozenEntry::new));

        EntityReference<?> entity();

        float pitch();

        float yaw();

        float radius();

        boolean removed();

        @Nullable UUID spellId();

        Set<UUID> claimants();
    }

    public record FrozenEntry<T extends Spell>(EntityReference<?> entity, boolean removed, float pitch, float yaw, float radius, @Nullable UUID spellId, Set<UUID> claimants) implements Entry { }

    public class MutableEntry<T extends Spell> implements Entry, PositionalDataMap.Hotspot {
        public final EntityReference<?> entity;

        @Nullable
        private UUID spellId;
        private WeakReference<T> spell;

        private boolean removed;

        private float pitch;
        private final AtomicBoolean changed = new AtomicBoolean(true);
        private float yaw;
        private float radius;

        private final Set<UUID> claimants = new HashSet<>();

        private Optional<BlockPos> currentPos = Optional.empty();
        private Optional<BlockPos> previousPos = Optional.empty();

        private MutableEntry(Entry entry) {
            this.entity = entry.entity();
            this.removed = entry.removed();
            this.pitch = entry.pitch();
            this.yaw = entry.yaw();
            this.radius = entry.radius();
            this.spellId = entry.spellId();
            this.claimants.addAll(entry.claimants());
            update();
        }

        public MutableEntry(T spell, Caster<?> caster) {
            this.entity = new EntityReference<>(caster.asEntity());
            this.spell = new WeakReference<>(spell);
            spellId = spell.getUuid();
            update();
        }

        void update() {
            previousPos = currentPos;
            currentPos = entity.getTarget().map(t -> BlockPos.ofFloored(t.pos()));
            if (!currentPos.equals(previousPos)) {
                positionData.update(this);
            }
        }

        public boolean hasChanged() {
            return changed.getAndSet(false);
        }

        @Override
        public EntityReference<?> entity() {
            return entity;
        }

        @Override
        public float pitch() {
            return pitch;
        }

        public void setPitch(float pitch) {
            if (!MathHelper.approximatelyEquals(this.pitch, pitch)) {
                this.pitch = pitch;
                changed.set(true);
            }
            markDirty();
        }

        @Override
        public float yaw() {
            return yaw;
        }

        public void setYaw(float yaw) {
            if (!MathHelper.approximatelyEquals(this.yaw, yaw)) {
                this.yaw = yaw;
                changed.set(true);
            }
            markDirty();
        }

        @Override
        public BlockPos getCenter() {
            return currentPos.orElse(BlockPos.ORIGIN);
        }

        @Override
        public float radius() {
            return radius;
        }

        public void setRadius(float radius) {
            if (!MathHelper.approximatelyEquals(this.radius, radius)) {
                this.radius = radius;
                if ((int)radius != (int)this.radius) {
                    positionData.update(this);
                }
                changed.set(true);
            }
            markDirty();
        }

        public boolean isAlive() {
            return !removed();
        }

        @Override
        public boolean removed() {
            if (!removed) {
                getSpell();
            }
            return removed;
        }

        @Override
        @Nullable
        public UUID spellId() {
            return spellId;
        }

        public void markDead() {
            Unicopia.LOGGER.debug("Marking " + entity.getTarget().orElse(null) + " as dead");
            removed = true;
            positionData.remove(this);
            claimants.clear();
            markDirty();
        }

        public boolean entityMatches(UUID uuid) {
            return entity.getTarget().filter(target -> uuid.equals(target.uuid())).isPresent();
        }

        public void claim(UUID claimant) {
            claimants.add(claimant);
            markDirty();
        }

        public void release(UUID claimant) {
            claimants.remove(claimant);
            markDirty();
        }

        public boolean isClaimedBy(UUID claimant) {
            return claimants.contains(claimant);
        }

        public boolean hasClaimant() {
            return !claimants.isEmpty();
        }

        @Override
        public Set<UUID> claimants() {
            return Set.copyOf(claimants);
        }

        @Nullable
        public T getSpell() {
            if (removed) {
                return null;
            }
            T spell = this.spell.get();
            if (spell == null) {
                if (spellId != null) {
                    spell = entity
                            .getOrEmpty(world)
                            .flatMap(Caster::of)
                            .flatMap(caster -> caster.getSpellSlot().<T>get(s -> s.getUuid().equals(spellId)))
                            .orElse(null);

                    if (spell != null) {
                        this.spell = new WeakReference<>(spell);
                    }
                }
            }

            if (spell != null && spell.isDead()) {
                spellId = null;
                spell = null;
                markDead();
            }

            return spell;
        }

        @Nullable
        public Caster<?> getCaster() {
            if (removed) {
                return null;
            }
            return Caster.of(this.entity.get(world)).orElse(null);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof MutableEntry<?> e
                    && e.entity.referenceEquals(entity)
                    && Objects.equals(e.spell.get(), spell.get());
        }

        public boolean equals(UUID entityId, UUID spellId) {
            return entity.referenceEquals(entityId) && spellId.equals(this.spellId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entity, spell.get());
        }
    }
}
