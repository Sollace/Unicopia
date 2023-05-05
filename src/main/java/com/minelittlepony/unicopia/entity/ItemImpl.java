package com.minelittlepony.unicopia.entity;

import java.util.List;

import com.minelittlepony.unicopia.*;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;
import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class ItemImpl implements Equine<ItemEntity> {
    private static final TrackedData<String> ITEM_RACE = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.STRING);
    static final TrackedData<Float> ITEM_GRAVITY = DataTracker.registerData(ItemEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private final ItemEntity entity;

    private final ItemPhysics physics;

    private Race serverRace;

    public ItemImpl(ItemEntity owner) {
        this.entity = owner;
        this.physics = new ItemPhysics(owner);
        owner.getDataTracker().startTracking(ITEM_GRAVITY, 1F);
        owner.getDataTracker().startTracking(ITEM_RACE, Race.REGISTRY.getId(Race.HUMAN).toString());
    }

    @Override
    public boolean onProjectileImpact(ProjectileEntity projectile) {
        return false;
    }

    @Override
    public boolean beforeUpdate() {

        if (!entity.world.isClient) {
            Race race = getSpecies();
            if (race != serverRace) {
                serverRace = race;
                setSpecies(Race.HUMAN);
                setSpecies(race);
            }
        }

        ItemStack stack = entity.getStack();
        IItemEntity i = (IItemEntity)entity;

        if (!stack.isEmpty()) {

            Item item = stack.getItem();
            ClingyItem clingy = item instanceof ClingyItem ? (ClingyItem)item : ClingyItem.DEFAULT;

            if (clingy.isClingy(stack)) {
                Random rng = entity.world.random;

                entity.world.addParticle(clingy.getParticleEffect((IItemEntity)entity),
                        entity.getX() + rng.nextFloat() - 0.5,
                        entity.getY() + rng.nextFloat() - 0.5,
                        entity.getZ() + rng.nextFloat() - 0.5,
                        0, 0, 0
                );

                Vec3d position = entity.getPos();
                VecHelper.findInRange(entity, entity.world, entity.getPos(), clingy.getFollowDistance(i), e -> e instanceof PlayerEntity)
                    .stream()
                    .sorted((a, b) -> (int)(a.getPos().distanceTo(position) - b.getPos().distanceTo(position)))
                    .findFirst()
                    .ifPresent(player -> {
                        double distance = player.getPos().distanceTo(entity.getPos());

                        entity.move(MovementType.SELF,  player.getPos().subtract(entity.getPos()).multiply(distance < 0.3 ? 1 : clingy.getFollowSpeed(i)));
                        if (entity.horizontalCollision) {
                            entity.move(MovementType.SELF, new Vec3d(0, entity.verticalCollision ? -0.3 : 0.3, 0));
                        }

                        clingy.interactWithPlayer(i, (PlayerEntity)player);
                    });
            }

            if (stack.isIn(UTags.FALLS_SLOWLY)) {
                if (!entity.isOnGround() && Math.signum(entity.getVelocity().y) != getPhysics().getGravitySignum()) {
                    double ticks = ((Entity)entity).age;
                    double shift = Math.sin(ticks / 9D) / 9D;
                    double rise = -Math.cos(ticks / 9D) * getPhysics().getGravitySignum();

                    entity.prevYaw = entity.prevYaw;
                    entity.setYaw(entity.getYaw() + 0.3F);

                    entity.setVelocity(
                            entity.getVelocity()
                                .multiply(0.25, 0, 0.25)
                                .add(0, rise, 0)
                                .add(entity.getRotationVec(1)).normalize().multiply(shift)
                    );
                }
            }

            if (stack.getItem() instanceof GroundTickCallback) {
                return ((GroundTickCallback)stack.getItem()).onGroundTick(i).isAccepted();
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
        return Race.fromName(entity.getDataTracker().get(ITEM_RACE), Race.HUMAN);
    }

    @Override
    public void setSpecies(Race race) {
        entity.getDataTracker().set(ITEM_RACE, Race.REGISTRY.getId(race).toString());
    }

    @Override
    public void toNBT(NbtCompound compound) {
        compound.putString("owner_race", Race.REGISTRY.getId(getSpecies()).toString());
        physics.toNBT(compound);
    }

    @Override
    public void fromNBT(NbtCompound compound) {
        if (compound.contains("owner_race", NbtElement.STRING_TYPE)) {
            setSpecies(Race.fromName(compound.getString("owner_race"), Race.HUMAN));
        }
        physics.fromNBT(compound);
    }

    @Override
    public ItemEntity asEntity() {
        return entity;
    }

    public static <T extends Item> T registerTickCallback(T item, GroundTickCallback callback) {
        ((ItemImpl.TickableItem)item).addGroundTickCallback(callback);
        return item;
    }

    public interface TickableItem extends GroundTickCallback {

        List<GroundTickCallback> getCallbacks();

        default void addGroundTickCallback(GroundTickCallback callback) {
            getCallbacks().add(callback);
        }

        @Override
        default ActionResult onGroundTick(IItemEntity entity) {
            for (var callback : getCallbacks()) {
                ActionResult result = callback.onGroundTick(entity);
                if (result.isAccepted()) {
                    return result;
                }
            }
            return ActionResult.PASS;
        }
    }

    public interface GroundTickCallback {
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
            return 6 * (1 + EnchantmentHelper.getLevel(UEnchantments.CLINGY, entity.get().asEntity().getStack()));
        }

        default float getFollowSpeed(IItemEntity entity) {
            return Math.min(1, 0.02F * (1 + EnchantmentHelper.getLevel(UEnchantments.CLINGY, entity.get().asEntity().getStack())));
        }

        default void interactWithPlayer(IItemEntity entity, PlayerEntity player) {

        }

        default void inFrameTick(ItemFrameEntity entity) {

        }
    }
}
