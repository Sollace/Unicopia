package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.CastingMethod;
import com.minelittlepony.unicopia.ability.magic.spell.HomingSpell;
import com.minelittlepony.unicopia.ability.magic.spell.Situation;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;
import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.behaviour.EntitySwap;
import com.minelittlepony.unicopia.entity.behaviour.Inventory;
import com.minelittlepony.unicopia.item.AlicornAmuletItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.GameMode;

public class MindSwapSpell extends MimicSpell implements ProjectileDelegate.EntityHitListener {

    private final EntityReference<LivingEntity> counterpart = new EntityReference<>();

    private Optional<Inventory> myStoredInventory = Optional.empty();
    private Optional<Inventory> theirStoredInventory = Optional.empty();

    private boolean initialized;

    protected MindSwapSpell(CustomisedSpellType<?> type) {
        super(type);
    }

    @Nullable
    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        if (caster.asEntity() instanceof LivingEntity living && isValidTarget(living)) {
            return null;
        }
        return method == CastingMethod.STAFF ? toThrowable() : this;
    }

    @Override
    protected void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (initialized && !caster.isClient()) {
            counterpart.ifPresent(caster.asWorld(), e -> {
                initialized = false;
                LivingEntity master = caster.getMaster();
                Caster<?> other = Caster.of(e).get();

                other.getSpellSlot().removeIf(SpellType.MIMIC);
                caster.getSpellSlot().removeIf(getType());

                if (!isValidTarget(master) || !isValidTarget(e)) {
                    master.damage(caster.asWorld().getDamageSources().magic(), Float.MAX_VALUE);
                    master.setHealth(0);
                    e.damage(caster.asWorld().getDamageSources().magic(), Float.MAX_VALUE);
                    e.setHealth(0);
                } else {
                    if (master instanceof ServerPlayerEntity sMaster && e instanceof ServerPlayerEntity sE) {
                        swapPlayerData(sMaster, sE);
                    } else {
                        EntitySwap.ALL.accept(e, master);
                        Inventory.swapInventories(
                                e, myStoredInventory.or(() -> Inventory.of(e)),
                                master, theirStoredInventory.or(() -> Inventory.of(master)),
                                a -> {},
                                a -> {}
                        );
                    }
                }

                other.playSound(USounds.SPELL_MINDSWAP_UNSWAP, 0.2F);
                caster.playSound(USounds.SPELL_MINDSWAP_UNSWAP, 0.2F);
            });
            counterpart.set(null);
        }
    }

    @Override
    public boolean tick(Caster<?> caster, Situation situation) {

        if (situation != Situation.BODY) {
            return true;
        }

        if (!caster.isClient()) {
            if (!initialized) {
                counterpart.ifPresent(caster.asWorld(), e -> {
                    if (!isValidTarget(e)) {
                        counterpart.set(null);
                        setDead();
                        return;
                    }

                    LivingEntity master = caster.getMaster();

                    if (!isValidTarget(master)) {
                        counterpart.set(null);
                        setDead();
                        return;
                    }

                    setDisguise(e);
                    Caster<?> other = Caster.of(e).get();
                    MimicSpell mimic = SpellType.MIMIC.withTraits().apply(other, CastingMethod.INDIRECT);
                    mimic.setHidden(true);
                    mimic.setDisguise(master);

                    if (master instanceof ServerPlayerEntity sMaster && e instanceof ServerPlayerEntity sE) {
                        swapPlayerData(sMaster, sE);
                    } else {
                        EntitySwap.ALL.accept(master, e);
                        Inventory.swapInventories(
                                master, Inventory.of(master),
                                e, Inventory.of(e),
                                a -> myStoredInventory = Optional.of(a),
                                a -> theirStoredInventory = Optional.of(a)
                        );
                    }

                    other.playSound(USounds.SPELL_MINDSWAP_SWAP, 1);
                    caster.playSound(USounds.SPELL_MINDSWAP_SWAP, 1);
                });
                initialized = true;
            }

            if (counterpart.isSet()) {
                LivingEntity other = counterpart.get(caster.asWorld());

                if (other == null) {
                    caster.getOriginatingCaster().asEntity().damage(caster.asWorld().getDamageSources().magic(), Float.MAX_VALUE);
                    destroy(caster);
                    return false;
                }

                if (!Caster.of(other).get().getSpellSlot().contains(SpellType.MIMIC)) {
                    destroy(caster);
                    return false;
                }
            }

            if (!caster.asEntity().isAlive()) {
                counterpart.ifPresent(caster.asWorld(), e -> {
                    e.damage(e.getDamageSources().magic(), Float.MAX_VALUE);
                });
                destroy(caster);
                return false;
            }
        }

        return super.tick(caster, situation);
    }


    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Caster.of(projectile.getMaster()).ifPresent(master -> {
            if (hit.getEntity() instanceof LivingEntity living
                    && isValidTarget(living)
                    && getTypeAndTraits().apply(master, CastingMethod.DIRECT) instanceof HomingSpell homing) {
                homing.setTarget(hit.getEntity());
            }
        });
    }

    @Override
    public boolean setTarget(Entity target) {
        if (target instanceof LivingEntity living) {
            counterpart.set(living);
        }

        return false;
    }

    protected boolean isValidTarget(LivingEntity entity) {
        return Caster.of(entity).isPresent()
                //&& !UItems.ALICORN_AMULET.isApplicable(entity)
                && !UItems.PEARL_NECKLACE.isApplicable(entity);
    }

    @Override
    public void toNBT(NbtCompound compound, WrapperLookup lookup) {
        super.toNBT(compound, lookup);
        compound.put("counterpart", counterpart.toNBT(lookup));
        compound.putBoolean("initialized", initialized);

        myStoredInventory.ifPresent(mine -> compound.put("myStoredInventory", NbtSerialisable.encode(Inventory.CODEC, mine, lookup)));
        theirStoredInventory.ifPresent(theirs -> compound.put("theirStoredInventory", NbtSerialisable.encode(Inventory.CODEC, theirs, lookup)));
    }

    @Override
    public void fromNBT(NbtCompound compound, WrapperLookup lookup) {
        super.fromNBT(compound, lookup);
        counterpart.fromNBT(compound.getCompound("counterpart"), lookup);
        initialized = compound.getBoolean("initialized");
        myStoredInventory = NbtSerialisable.decode(Inventory.CODEC, compound.getCompound("myStoredInventory"), lookup);
        theirStoredInventory = NbtSerialisable.decode(Inventory.CODEC, compound.getCompound("theirStoredInventory"), lookup);
    }

    private static void swapPlayerData(ServerPlayerEntity a, ServerPlayerEntity b) {
        AlicornAmuletItem.updateAttributes(Living.living(a), 0);
        AlicornAmuletItem.updateAttributes(Living.living(b), 0);

        final GameMode aMode = a.interactionManager.getGameMode();
        final GameMode bMode = b.interactionManager.getGameMode();

        final UUID aUUid = a.getUuid();
        final UUID bUUid = b.getUuid();

        final byte aModelBits = a.getDataTracker().get(PlayerAccess.getModelBitFlag());
        final byte bModelBits = b.getDataTracker().get(PlayerAccess.getModelBitFlag());

        final ServerPlayerEntity aClone = clonePlayer(a);
        final ServerPlayerEntity bClone = clonePlayer(b);

        aClone.getAbilities().creativeMode = a.getAbilities().creativeMode;
        bClone.getAbilities().creativeMode = b.getAbilities().creativeMode;

        a.copyFrom(bClone, true);
        b.copyFrom(aClone, true);

        a.setUuid(aUUid);
        b.setUuid(bUUid);

        a.getDataTracker().set(PlayerAccess.getModelBitFlag(), aModelBits);
        b.getDataTracker().set(PlayerAccess.getModelBitFlag(), bModelBits);

        a.interactionManager.changeGameMode(aMode);
        b.interactionManager.changeGameMode(bMode);

        a.deathTime = 0;
        b.deathTime = 0;

        EntitySwap.POSITION.accept(a, b);
        EntitySwap.VELOCITY.accept(a, b);
        EntitySwap.PITCH.accept(a, b);
        EntitySwap.YAW.accept(a, b);

        a.sendAbilitiesUpdate();
        b.sendAbilitiesUpdate();

        Living.updateVelocity(a);
        Living.updateVelocity(b);
    }

    private static ServerPlayerEntity clonePlayer(ServerPlayerEntity player) {
        ServerPlayerEntity clone = new FakePlayer(player.getServerWorld(), player.getGameProfile()) {
            @Override
            public void tick() {
                discard();
            }

            @Override
            public void setUuid(UUID uuid) {
                super.setUuid(UUID.randomUUID());
            }
        };

        NbtCompound compound = player.writeNbt(new NbtCompound());
        compound.remove("Dimension");
        compound.getCompound("unicopia_caster").remove("spells");
        clone.readNbt(compound);
        return clone;
    }
}
