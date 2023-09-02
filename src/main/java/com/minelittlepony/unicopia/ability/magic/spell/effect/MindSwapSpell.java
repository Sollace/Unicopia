package com.minelittlepony.unicopia.ability.magic.spell.effect;

import java.util.Optional;
import java.util.UUID;

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
import com.minelittlepony.unicopia.projectile.MagicProjectileEntity;
import com.minelittlepony.unicopia.projectile.ProjectileDelegate;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

    @Override
    public Spell prepareForCast(Caster<?> caster, CastingMethod method) {
        return method == CastingMethod.STAFF ? toThrowable() : this;
    }

    @Override
    public void onDestroyed(Caster<?> caster) {
        super.onDestroyed(caster);
        if (initialized && !caster.isClient()) {
            counterpart.ifPresent(caster.asWorld(), e -> {
                initialized = false;
                LivingEntity master = caster.getMaster();

                Caster<?> other = Caster.of(e).get();
                other.getSpellSlot().removeIf(SpellType.MIMIC, true);
                caster.getSpellSlot().removeIf(getType(), true);

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
                    LivingEntity master = caster.getMaster();

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
                setDirty();
            }

            if (counterpart.isSet()) {
                LivingEntity other = counterpart.get(caster.asWorld());

                if (other == null) {
                    caster.getOriginatingCaster().asEntity().damage(caster.asWorld().getDamageSources().magic(), Float.MAX_VALUE);
                    setDead();
                    return false;
                }

                if (!Caster.of(other).get().getSpellSlot().contains(SpellType.MIMIC)) {
                    onDestroyed(caster);
                    setDead();
                    return false;
                }
            }

            if (!caster.asEntity().isAlive()) {
                counterpart.ifPresent(caster.asWorld(), e -> {
                    e.damage(e.getDamageSources().magic(), Float.MAX_VALUE);
                });
                onDestroyed(caster);
                setDead();
                return false;
            }
        }

        return super.tick(caster, situation);
    }


    @Override
    public void onImpact(MagicProjectileEntity projectile, EntityHitResult hit) {
        Caster.of(projectile.getMaster()).ifPresent(master -> {
            if (getTypeAndTraits().apply(master, CastingMethod.DIRECT) instanceof HomingSpell homing) {
                homing.setTarget(hit.getEntity());
            }
        });
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

    private static void swapPlayerData(ServerPlayerEntity a, ServerPlayerEntity b) {

        final GameMode aMode = a.interactionManager.getGameMode();
        final GameMode bMode = b.interactionManager.getGameMode();

        final UUID aUUid = a.getUuid();
        final UUID bUUid = b.getUuid();

        final ServerPlayerEntity aClone = clonePlayer(a);
        final ServerPlayerEntity bClone = clonePlayer(b);

        aClone.getAbilities().creativeMode = a.getAbilities().creativeMode;
        bClone.getAbilities().creativeMode = b.getAbilities().creativeMode;

        a.copyFrom(bClone, true);
        b.copyFrom(aClone, true);

        a.setUuid(aUUid);
        b.setUuid(bUUid);

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











