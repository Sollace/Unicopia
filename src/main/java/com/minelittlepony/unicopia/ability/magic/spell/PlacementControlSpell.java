package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.effect.AbstractSpell;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PlacementControlSpell extends AbstractSpell implements OrientedSpell {
    @Nullable
    private UUID placedEntityId;

    private Optional<RegistryKey<World>> dimension = Optional.empty();
    private Optional<Vec3d> position = Optional.empty();
    private Optional<Vec3d> orientation = Optional.empty();

    @Nullable
    private Spell delegate;

    public PlacementControlSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    PlacementControlSpell(CustomisedSpellType<?> type, Spell delegate) {
        this(type);
        this.delegate = delegate;
    }

    @Nullable
    public Spell getDelegate() {
        return delegate;
    }

    public Optional<Vec3d> getPosition() {
        return position;
    }

    public void setDimension(RegistryKey<World> dimension) {
        this.dimension = Optional.of(dimension);
    }

    public void setPosition(Vec3d position) {
        this.position = Optional.of(position);
    }

    @Override
    public void setOrientation(Caster<?> caster, float pitch, float yaw) {
        this.orientation = Optional.of(new Vec3d(pitch, yaw, 0));
        if (delegate instanceof OrientedSpell o) {
            o.setOrientation(caster, pitch, yaw);
        }
        setDirty();
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
        if (delegate == null) {
            return false;
        }
        boolean result = super.apply(caster);
        if (result) {
            if (dimension.isEmpty()) {
                setDimension(caster.asWorld().getRegistryKey());
            }
            if (position.isEmpty()) {
                setPosition(caster.asEntity().getPos());
            }
            if (delegate instanceof PlacementDelegate) {
                ((PlacementDelegate)delegate).onPlaced(caster, this);
            }

            CastSpellEntity entity = new CastSpellEntity(caster.asWorld(), caster, this);

            Vec3d pos = position.get();
            Vec3d rot = orientation.orElse(Vec3d.ZERO);

            entity.updatePositionAndAngles(pos.x, pos.y, pos.z, (float)rot.y, (float)rot.x);
            entity.getWorld().spawnEntity(entity);

            placedEntityId = entity.getUuid();
            setDirty();
        }
        return result;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.isClient() && getConnection(source) == null) {
            setDead();
        }
        return !isDead();
    }

    @Nullable
    private Ether.Entry<?> getConnection(Caster<?> source) {
        return delegate == null || placedEntityId == null ? null : getWorld(source)
                .map(world -> Ether.get(world).get(getDelegate().getTypeAndTraits().type(), placedEntityId, delegate.getUuid()))
                .orElse(null);
    }

    private Optional<World> getWorld(Caster<?> source) {
        return dimension.map(source.asWorld().getServer()::getWorld);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("spell", Spell.writeNbt(delegate));
        position.ifPresent(pos -> compound.put("position", NbtSerialisable.writeVector(pos)));
        orientation.ifPresent(o -> compound.put("orientation", NbtSerialisable.writeVector(o)));
        dimension.ifPresent(d -> compound.putString("dimension", d.getValue().toString()));
        if (placedEntityId != null) {
            compound.putUuid("placedEntityId", placedEntityId);
        }
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        delegate = Spell.readNbt(compound.getCompound("spell"));
        placedEntityId = compound.containsUuid("placedEntityId") ? compound.getUuid("placedEntityId") : null;
        position = compound.contains("position") ? Optional.of(NbtSerialisable.readVector(compound.getList("position", NbtElement.FLOAT_TYPE))) : Optional.empty();
        orientation = compound.contains("orientation") ? Optional.of(NbtSerialisable.readVector(compound.getList("orientation", NbtElement.FLOAT_TYPE))) : Optional.empty();
        if (compound.contains("dimension", NbtElement.STRING_TYPE)) {
            dimension = Optional.ofNullable(Identifier.tryParse(compound.getString("dimension"))).map(id -> RegistryKey.of(RegistryKeys.WORLD, id));
        }
    }

    public interface PlacementDelegate {
        void onPlaced(Caster<?> source, PlacementControlSpell parent);
    }
}
