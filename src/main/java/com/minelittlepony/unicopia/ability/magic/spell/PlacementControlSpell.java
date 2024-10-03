package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.AbstractSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.network.track.DataTracker;
import com.minelittlepony.unicopia.network.track.TrackableDataType;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PlacementControlSpell extends AbstractSpell implements OrientedSpell {
    private final DataTracker.Entry<UUID> placedEntityId = dataTracker.startTracking(TrackableDataType.UUID, null);
    private final DataTracker.Entry<Optional<RegistryKey<World>>> dimension = dataTracker.startTracking(TrackableDataType.ofRegistryKey(), Optional.empty());
    private final DataTracker.Entry<Optional<Vec3d>> position = dataTracker.startTracking(TrackableDataType.OPTIONAL_VECTOR, Optional.empty());
    private final DataTracker.Entry<Optional<Vec3d>> orientation = dataTracker.startTracking(TrackableDataType.OPTIONAL_VECTOR, Optional.empty());

    @Nullable
    private Spell delegate;

    public PlacementControlSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    PlacementControlSpell(Spell delegate) {
        this(SpellType.PLACE_CONTROL_SPELL.withTraits(delegate.getTypeAndTraits().traits()));
        this.delegate = delegate;
    }

    @Nullable
    public Spell getDelegate() {
        return delegate;
    }

    public Optional<Vec3d> getPosition() {
        return position.get();
    }

    public void setDimension(RegistryKey<World> dimension) {
        this.dimension.set(Optional.of(dimension));
    }

    public void setPosition(Vec3d position) {
        this.position.set(Optional.of(position));
    }

    @Override
    public void setOrientation(Caster<?> caster, float pitch, float yaw) {
        this.orientation.set(Optional.of(new Vec3d(pitch, yaw, 0)));
        if (delegate instanceof OrientedSpell o) {
            o.setOrientation(caster, pitch, yaw);
        }
        if (!caster.isClient()) {
            var entry = getConnection(caster);
            if (entry != null) {
                entry.setPitch(pitch);
                entry.setYaw(yaw);
            }
        }
    }

    @Override
    public boolean apply(Caster<?> caster) {
        return delegate != null && super.apply(caster);
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.isClient()) {

            if (placedEntityId.get() == null) {
                if (dimension.get().isEmpty()) {
                    setDimension(source.asWorld().getRegistryKey());
                }
                if (getPosition().isEmpty()) {
                    setPosition(source.asEntity().getPos());
                }

                CastSpellEntity entity = new CastSpellEntity(source.asWorld(), source, this);

                Vec3d pos = getPosition().get();
                Vec3d rot = orientation.get().orElse(Vec3d.ZERO);

                entity.updatePositionAndAngles(pos.x, pos.y, pos.z, (float)rot.y, (float)rot.x);
                entity.getWorld().spawnEntity(entity);

                placedEntityId.set(entity.getUuid());
            } else {
                if (getConnection(source) == null) {
                    setDead();
                }
            }
        }

        return !isDead();
    }

    @Nullable
    private Ether.Entry<?> getConnection(Caster<?> source) {
        return delegate == null || placedEntityId.get() == null ? null : getWorld(source)
                .map(world -> Ether.get(world).get(getDelegate().getTypeAndTraits().type(), placedEntityId.get(), delegate.getUuid()))
                .orElse(null);
    }

    private Optional<World> getWorld(Caster<?> source) {
        return dimension.get().map(source.asWorld().getServer()::getWorld);
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.put("spell", Spell.writeNbt(delegate, lookup));
        position.get().ifPresent(pos -> compound.put("position", NbtSerialisable.writeVector(pos)));
        orientation.get().ifPresent(o -> compound.put("orientation", NbtSerialisable.writeVector(o)));
        dimension.get().ifPresent(d -> compound.putString("dimension", d.getValue().toString()));
        if (placedEntityId.get() != null) {
            compound.putUuid("placedEntityId", placedEntityId.get());
        }
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        delegate = Spell.readNbt(compound.getCompound("spell"), lookup);
        placedEntityId.set(compound.containsUuid("placedEntityId") ? compound.getUuid("placedEntityId") : null);
        position.set(compound.contains("position") ? Optional.of(NbtSerialisable.readVector(compound.getList("position", NbtElement.DOUBLE_TYPE))) : Optional.empty());
        orientation.set(compound.contains("orientation") ? Optional.of(NbtSerialisable.readVector(compound.getList("orientation", NbtElement.DOUBLE_TYPE))) : Optional.empty());
        dimension.set(compound.contains("dimension", NbtElement.STRING_TYPE) ? Optional.ofNullable(Identifier.tryParse(compound.getString("dimension"))).map(id -> RegistryKey.of(RegistryKeys.WORLD, id)) : Optional.empty());
    }

    public interface PlacementDelegate {
        void onPlaced(Caster<?> source, PlacementControlSpell parent);
    }
}
