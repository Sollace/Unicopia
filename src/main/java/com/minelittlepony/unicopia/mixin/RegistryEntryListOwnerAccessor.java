package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;

@Mixin(value = { RegistryEntryList.Named.class })
public interface RegistryEntryListOwnerAccessor<T> {
    @Accessor("owner")
    RegistryEntryOwner<T> getOwner();
}

@Mixin(value = { RegistryEntry.Reference.class })
abstract class MixinRegistryEntryReference<T> implements RegistryEntryListOwnerAccessor<T> {
    @Override
    @Accessor("owner")
    public abstract RegistryEntryOwner<T> getOwner();
}