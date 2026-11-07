package com.minelittlepony.unicopia.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.behaviour.Guest;
import com.minelittlepony.unicopia.network.track.DataTrackerManager;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class BaseEquine<T extends Entity> implements Equine<T>, Guest {
    protected final T entity;

    private final List<Tickable> tickers = new ArrayList<>();

    private final DataTrackerManager dataTrackerManager;
    private final Transportation transportation;

    @Nullable
    private transient Caster<?> host;

    public BaseEquine(EntityConvertable<T> entity) {
        this.entity = entity.asEntity();
        this.transportation = new Transportation(entity);
        this.dataTrackerManager = new DataTrackerManager(entity);
    }

    public DataTrackerManager getDataTrackerManager() {
        return dataTrackerManager;
    }

    public <Q extends Tickable> Q addTicker(Q tickable) {
        tickers.add(Objects.requireNonNull(tickable, "tickable cannot be null"));
        return tickable;
    }

    @Override
    public T asEntity() {
        return entity;
    }

    @Override
    public final Transportation getTransportation() {
        return transportation;
    }

    @Override
    public void setHost(@Nullable Caster<?> host) {
        this.host = host;
    }

    @Override
    public @Nullable Caster<?> getHost() {
        return host;
    }

    @Override
    public boolean beforeUpdate() {
        return false;
    }

    @Override
    public void tick() {
        tickers.forEach(Tickable::tick);
        transportation.tick();
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        return false;
    }

    @Override
    public Race getSpecies() {
        return Race.HUMAN;
    }

    @Override
    public void setSpecies(Race race) {

    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
    }
}
