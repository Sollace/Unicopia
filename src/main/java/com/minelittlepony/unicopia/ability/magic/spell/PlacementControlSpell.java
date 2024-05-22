package com.minelittlepony.unicopia.ability.magic.spell;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.SpellPredicate;
import com.minelittlepony.unicopia.ability.magic.spell.effect.CustomisedSpellType;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.EntityReference.EntityValues;
import com.minelittlepony.unicopia.entity.mob.CastSpellEntity;
import com.minelittlepony.unicopia.entity.mob.UEntities;
import com.minelittlepony.unicopia.server.world.Ether;
import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PlacementControlSpell extends AbstractDelegatingSpell implements OrientedSpell {
    @Nullable
    private UUID placedSpellId;
    private final EntityReference<CastSpellEntity> castEntity = new EntityReference<>();

    private Optional<RegistryKey<World>> dimension = Optional.empty();
    private Optional<Vec3d> position = Optional.empty();
    private Optional<Vec3d> orientation = Optional.empty();

    public PlacementControlSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    PlacementControlSpell(CustomisedSpellType<?> type, Spell delegate) {
        super(type);
        this.delegate.set(delegate);
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
        castEntity.ifPresent(caster.asWorld(), entity -> {
            entity.getSpellSlot().stream(SpellPredicate.IS_ORIENTED).forEach(spell -> {
                if (!getTypeAndTraits().type().test(spell)) {
                    spell.setOrientation(caster, pitch, yaw);
                }
            });
        });
        setDirty();
    }

    @Override
    public boolean apply(Caster<?> caster) {
        boolean result = super.apply(caster);
        if (result) {
            if (!caster.isClient()) {
                Ether.get(caster.asWorld()).getOrCreate(this, caster);
            }
            if (dimension.isEmpty()) {
                setDimension(caster.asWorld().getRegistryKey());
            }
            if (position.isEmpty()) {
                setPosition(caster.asEntity().getPos());
            }
        }
        return result;
    }

    @Override
    public boolean tick(Caster<?> source, Situation situation) {
        if (!source.isClient()) {
            Ether.get(source.asWorld()).getOrCreate(this, source);
            castEntity.getTarget().ifPresentOrElse(target -> {
                if (!checkConnection(source, target)) {
                    setDead();
                }
            }, () -> spawnPlacedEntity(source));
        }
        return !isDead();
    }

    private void spawnPlacedEntity(Caster<?> source) {
        PlaceableSpell copy = new PlaceableSpell(source, this, getDelegate());

        Vec3d pos = position.orElse(source.asEntity().getPos());
        Vec3d rot = orientation.orElse(Vec3d.ZERO);

        CastSpellEntity entity = UEntities.CAST_SPELL.create(source.asWorld());
        entity.setCaster(source);
        entity.updatePositionAndAngles(pos.x, pos.y, pos.z, (float)rot.y, (float)rot.x);
        entity.setYaw((float)rot.y);
        entity.setPitch((float)rot.x);
        copy.apply(entity);
        entity.getWorld().spawnEntity(entity);

        placedSpellId = copy.getUuid();
        castEntity.set(entity);
        setDirty();
    }

    private boolean checkConnection(Caster<?> source, EntityValues<?> target) {
        return getWorld(source)
                .map(Ether::get)
                .map(ether -> ether.get(SpellType.PLACED_SPELL, target, placedSpellId))
                .isPresent();
    }

    private Optional<World> getWorld(Caster<?> source) {
        return dimension.map(source.asWorld().getServer()::getWorld);
    }

    @Override
    protected void onDestroyed(Caster<?> source) {
        if (!source.isClient()) {
            Ether.get(source.asWorld()).remove(this, source);
        }
        super.onDestroyed(source);
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        position.ifPresent(pos -> compound.put("position", NbtSerialisable.writeVector(pos)));
        orientation.ifPresent(o -> compound.put("orientation", NbtSerialisable.writeVector(o)));
        dimension.ifPresent(d -> compound.putString("dimension", d.getValue().toString()));
        if (placedSpellId != null) {
            compound.putUuid("placedSpellId", placedSpellId);
        }
        compound.put("castEntity", castEntity.toNBT());

    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        placedSpellId = compound.containsUuid("placedSpellId") ? compound.getUuid("placedSpellId") : null;
        position = compound.contains("position") ? Optional.of(NbtSerialisable.readVector(compound.getList("position", NbtElement.FLOAT_TYPE))) : Optional.empty();
        orientation = compound.contains("orientation") ? Optional.of(NbtSerialisable.readVector(compound.getList("orientation", NbtElement.FLOAT_TYPE))) : Optional.empty();
        if (compound.contains("dimension", NbtElement.STRING_TYPE)) {
            dimension = Optional.ofNullable(Identifier.tryParse(compound.getString("dimension"))).map(id -> RegistryKey.of(RegistryKeys.WORLD, id));
        }
        castEntity.fromNBT(compound.getCompound("castEntity"));
    }

    public interface PlacementDelegate {
        void onPlaced(Caster<?> source, PlacementControlSpell parent);
    }
}
