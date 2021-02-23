package com.minelittlepony.unicopia.entity;

import java.util.Random;

import com.minelittlepony.unicopia.Owned;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public class ItemImpl implements Equine<ItemEntity>, Owned<ItemEntity> {
    private static final TrackedData<Integer> ITEM_RACE = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    static final TrackedData<Float> ITEM_GRAVITY = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private final ItemEntity owner;

    private final ItemPhysics physics = new ItemPhysics(this);

    private Race serverRace;

    public ItemImpl(ItemEntity owner) {
        this.owner = owner;
        owner.getDataTracker().startTracking(ITEM_GRAVITY, 1F);
        owner.getDataTracker().startTracking(ITEM_RACE, Race.HUMAN.ordinal());
    }

    @Override
    public boolean beforeUpdate() {

        if (!owner.world.isClient) {
            Race race = getSpecies();
            if (race != serverRace) {
                serverRace = race;
                setSpecies(Race.HUMAN);
                setSpecies(race);
            }
        }

        ItemStack stack = owner.getStack();

        if (!stack.isEmpty()) {

            Item item = stack.getItem();
            ClingyItem clingy = item instanceof ClingyItem ? (ClingyItem)item : ClingyItem.DEFAULT;

            IItemEntity i = (IItemEntity)owner;

            if (clingy.isClingy(stack)) {
                Random rng = owner.world.random;

                owner.world.addParticle(clingy.getParticleEffect((IItemEntity)owner),
                        owner.getX() + rng.nextFloat() - 0.5,
                        owner.getY() + rng.nextFloat() - 0.5,
                        owner.getZ() + rng.nextFloat() - 0.5,
                        0, 0, 0
                );

                Vec3d position = owner.getPos();
                VecHelper.findInRange(owner, owner.world, owner.getPos(), clingy.getFollowDistance(i), e -> e instanceof PlayerEntity)
                    .stream()
                    .sorted((a, b) -> (int)(a.getPos().distanceTo(position) - b.getPos().distanceTo(position)))
                    .findFirst()
                    .ifPresent(player -> {
                        owner.move(MovementType.SELF, player.getPos().subtract(owner.getPos()).multiply(clingy.getFollowSpeed(i)));
                        if (owner.horizontalCollision) {
                            owner.move(MovementType.SELF, new Vec3d(0, owner.verticalCollision ? -0.3 : 0.3, 0));
                        }

                        clingy.interactWithPlayer(i, (PlayerEntity)player);
                    });
            }

            if (stack.getItem() instanceof TickableItem) {
                return ((TickableItem)stack.getItem()).onGroundTick(i) == ActionResult.SUCCESS;
            }
        }


        return false;
    }

    @Override
    public void tick() {
        physics.tick();
    }

    @Override
    public Physics getPhysics() {
        return physics;
    }

    @Override
    public Race getSpecies() {
        return Race.fromId(getMaster().getDataTracker().get(ITEM_RACE));
    }

    @Override
    public void setSpecies(Race race) {
        getMaster().getDataTracker().set(ITEM_RACE, race.ordinal());
    }

    @Override
    public void toNBT(CompoundTag compound) {
        compound.putString("owner_species", getSpecies().name());
        physics.toNBT(compound);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        setSpecies(Race.fromName(compound.getString("owner_species")));
        physics.fromNBT(compound);
    }

    @Override
    public void setMaster(ItemEntity owner) {

    }

    @Override
    public ItemEntity getMaster() {
        return owner;
    }

    public interface TickableItem {
        ActionResult onGroundTick(IItemEntity entity);
    }

    public interface ClingyItem {
        ClingyItem DEFAULT = stack -> {
            return EnchantmentHelper.getLevel(UEnchantments.CLINGY, stack) > 0;
        };

        boolean isClingy(ItemStack stack);

        default ParticleEffect getParticleEffect(IItemEntity entity) {
            return ParticleTypes.AMBIENT_ENTITY_EFFECT;
        }

        default float getFollowDistance(IItemEntity entity) {
            return 6 * (1 + EnchantmentHelper.getLevel(UEnchantments.CLINGY, entity.get().getMaster().getStack()));
        }

        default float getFollowSpeed(IItemEntity entity) {
            return Math.min(1, 0.02F * (1 + EnchantmentHelper.getLevel(UEnchantments.CLINGY, entity.get().getMaster().getStack())));
        }

        default void interactWithPlayer(IItemEntity entity, PlayerEntity player) {

        }
    }
}
