package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.behaviour.EntitySwap;
import com.minelittlepony.unicopia.entity.behaviour.Inventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class MindSwapSpell extends MimicSpell {

    private final EntityReference<LivingEntity> counterpart = new EntityReference<>();

    private Optional<Inventory> myStoredInventory = Optional.empty();
    private Optional<Inventory> theirStoredInventory = Optional.empty();

    private boolean initialized;

    protected MindSwapSpell(CustomisedSpellType<?> type) {
        super(type);
    }


    @Override
    public void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (initialized && !caster.isClient()) {
            counterpart.ifPresent(caster.asWorld(), e -> {
                LivingEntity master = caster.getMaster();

                EntitySwap.ALL.accept(e, master);
                Inventory.swapInventories(
                        e, myStoredInventory.or(() -> Inventory.of(e)),
                        master, theirStoredInventory.or(() -> Inventory.of(master)),
                        a -> {},
                        a -> {}
                );

                Caster<?> other = Caster.of(e).get();
                other.getSpellSlot().removeIf(SpellType.MIMIC, true);

                other.playSound(USounds.SPELL_MINDSWAP_UNSWAP, 1);
                caster.playSound(USounds.SPELL_MINDSWAP_UNSWAP, 1);
            });
            counterpart.set(null);
        }
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {

        if (!caster.isClient()) {
            if (!initialized) {
                initialized = true;
                setDirty();
                counterpart.ifPresent(caster.asWorld(), e -> {
                    LivingEntity master = caster.getMaster();

                    setDisguise(e);
                    Caster<?> other = Caster.of(e).get();
                    SpellType.MIMIC.withTraits().apply(other).setDisguise(master);

                    EntitySwap.ALL.accept(master, e);
                    Inventory.swapInventories(
                            master, Inventory.of(master),
                            e, Inventory.of(e),
                            a -> myStoredInventory = Optional.of(a),
                            a -> theirStoredInventory = Optional.of(a)
                    );

                    other.playSound(USounds.SPELL_MINDSWAP_SWAP, 1);
                    caster.playSound(USounds.SPELL_MINDSWAP_SWAP, 1);
                });
            }

            if (counterpart.getId().isPresent() && counterpart.get(caster.asWorld()) == null) {
                caster.getOriginatingCaster().asEntity().damage(DamageSource.MAGIC, Float.MAX_VALUE);
                setDead();
                return false;
            }

            if (!caster.asEntity().isAlive()) {
                counterpart.ifPresent(caster.asWorld(), e -> {
                    e.damage(DamageSource.MAGIC, Float.MAX_VALUE);
                });
                onDestroyed(caster);
                setDead();
                return false;
            }
        }

        return super.tick(caster, situation);
    }

    @Override
    public boolean setTarget(Entity target) {
        if (target instanceof LivingEntity living && Caster.of(target).isPresent()) {
            counterpart.set(living);
        }

        return false;
    }

    @Override
    public void toNBT(NbtCompound compound) {
        super.toNBT(compound);
        compound.put("counterpart", counterpart.toNBT());
        compound.putBoolean("initialized", initialized);
        myStoredInventory.ifPresent(mine -> compound.put("myStoredInventory", mine.toNBT(new NbtCompound())));
        theirStoredInventory.ifPresent(mine -> compound.put("theirStoredInventory", mine.toNBT(new NbtCompound())));
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        super.fromNBT(compound);
        counterpart.fromNBT(compound.getCompound("counterpart"));
        initialized = compound.getBoolean("initialized");
        myStoredInventory = Optional.ofNullable(compound.contains("myStoredInventory", NbtElement.COMPOUND_TYPE) ? Inventory.fromNBT(compound.getCompound("myStoredInventory")) : null);
        theirStoredInventory = Optional.ofNullable(compound.contains("theirStoredInventory", NbtElement.COMPOUND_TYPE) ? Inventory.fromNBT(compound.getCompound("theirStoredInventory")) : null);
    }
}











