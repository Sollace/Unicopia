package com.minelittlepony.unicopia.entity.behaviour;

import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.entity.ItemWielder;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.mixin.MixinEntity;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class EntityBehaviour<T extends Entity> {

    private static final EntityBehaviour<Entity> DEFAULT = new EntityBehaviour<>();
    private static final Registry<EntityBehaviour<?>> REGISTRY = Registries.createSimple(new Identifier("unicopia", "entity_behaviour"));

    /**
     * Equivalent of the entity#tick method. Called every tick to update th logic for a disguise.
     * <br>
     * We use this to add entity-specific behaviours.
     */
    public void update(Caster<?> source, T entity, Disguise spell) {
        if (source instanceof Pony) {
            update((Pony)source, entity, spell);
        }
    }

    protected void update(Pony pony, T entity, Disguise spell) {

    }

    public void onImpact(Caster<?> source, T entity, float distance, float damageMultiplier, DamageSource cause) {

    }

    public T onCreate(T entity, EntityAppearance context, boolean wasNew) {
        entity.extinguish();
        return entity;
    }

    public void onDestroy(T entity) {
        entity.setInvulnerable(false);
        entity.setNoGravity(false);
        entity.remove(RemovalReason.KILLED);
    }

    public Optional<Double> getCameraDistance(Entity entity, Pony player) {
        if (entity == null) {
            return Optional.empty();
        }

        double normalHeight = PlayerEntity.STANDING_DIMENSIONS.height;
        double entityHeight = entity.getDimensions(entity.getPose()).height;

        return Optional.of(entityHeight / normalHeight);
    }

    public Optional<EntityDimensions> getDimensions(T entity, Optional<EntityDimensions> current) {
        if (entity == null) {
            return Optional.empty();
        }

        EntityDimensions dims = entity.getDimensions(entity.getPose());

        float h = Math.max(0.001F, dims.height);
        float w = Math.max(0.001F, dims.width);

        if (current.isPresent() && h == current.get().height && w == current.get().width) {
            return current;
        }

        return Optional.of(EntityDimensions.changing(w, h));
    }

    public void copyBaseAttributes(LivingEntity from, Entity to) {
        copyBaseAttributes(from, to, Vec3d.ZERO);
    }

    public void copyBaseAttributes(LivingEntity from, Entity to, Vec3d positionOffset) {
        // Set first because position calculations rely on it
        to.age = from.age;
        ((MixinEntity)to).setRemovalReason(from.getRemovalReason());
        to.setOnGround(from.isOnGround());

        if (!from.world.isClient) {
            // player collision is not known on the server
            boolean clip = to.noClip;
            to.noClip = false;
            Vec3d vel = from.getRotationVec(1);

            if (!(to instanceof AbstractDecorationEntity)) {
                to.move(MovementType.SELF, vel);
            } else {
                to.setVelocity(Vec3d.ZERO);
            }
            to.noClip = clip;
        } else {
            to.verticalCollision = from.verticalCollision;
            to.horizontalCollision = from.horizontalCollision;
        }

        if (EntityAppearance.isAxisAligned(to)) {
            double x = positionOffset.x + Math.floor(from.getX()) + 0.5;
            double y = positionOffset.y + Math.floor(from.getY());
            double z = positionOffset.z + Math.floor(from.getZ()) + 0.5;

            BlockPos pos = new BlockPos(x, y, z);

            if (!from.world.isAir(pos) && !from.world.isWater(pos)) {
                y++;
            }

            to.prevX = x;
            to.prevY = y;
            to.prevZ = z;

            to.lastRenderX = x;
            to.lastRenderY = y;
            to.lastRenderZ = z;

            to.updatePosition(x, y, z);

            if (to instanceof FallingBlockEntity) {
                ((FallingBlockEntity)to).setFallingBlockPos(from.getBlockPos());
            }
        } else {
            to.copyPositionAndRotation(from);

            to.prevX = positionOffset.x + from.prevX;
            to.prevY = positionOffset.y + from.prevY;
            to.prevZ = positionOffset.z + from.prevZ;

            to.lastRenderX = positionOffset.x + from.lastRenderX;
            to.lastRenderY = positionOffset.y + from.lastRenderY;
            to.lastRenderZ = positionOffset.z + from.lastRenderZ;
        }

        to.setVelocity(from.getVelocity());

        to.setPitch(from.getPitch());
        to.prevPitch = from.prevPitch;
        to.setYaw(from.getYaw());
        to.prevYaw = from.prevYaw;
        to.horizontalSpeed = from.horizontalSpeed;
        to.prevHorizontalSpeed = from.prevHorizontalSpeed;
        to.setOnGround(from.isOnGround());
        to.setInvulnerable(from.isInvulnerable() || (from instanceof PlayerEntity && ((PlayerEntity)from).getAbilities().creativeMode));

        to.distanceTraveled = from.distanceTraveled;

        if (to instanceof LivingEntity) {
            LivingEntity l = (LivingEntity)to;

            l.headYaw = from.headYaw;
            l.prevHeadYaw = from.prevHeadYaw;
            l.bodyYaw = from.bodyYaw;
            l.prevBodyYaw = from.prevBodyYaw;

            l.limbDistance = from.limbDistance;
            l.limbAngle = from.limbAngle;
            l.lastLimbDistance = from.lastLimbDistance;

            l.handSwingProgress = from.handSwingProgress;
            l.lastHandSwingProgress = from.lastHandSwingProgress;
            l.handSwingTicks = from.handSwingTicks;
            l.handSwinging = from.handSwinging;

            l.hurtTime = from.hurtTime;
            l.deathTime = from.deathTime;
            l.stuckStingerTimer = from.stuckStingerTimer;
            l.stuckArrowTimer = from.stuckArrowTimer;

            // disguise uses our health
            l.setHealth((from.getHealth() / from.getMaxHealth()) * l.getMaxHealth());

            // we use the disguise's air so changelings disguised as dolphin/axolotl/etc drown on land
            from.setAir((l.getAir() / l.getMaxAir()) * from.getMaxAir());

            copyInventory(from, l);
        }

        if (to instanceof TameableEntity) {
            ((TameableEntity)to).setSitting(from.isSneaking());
        }

        if (to instanceof AbstractSkeletonEntity) {
            ((AbstractSkeletonEntity)to).setAttacking(from.getItemUseTimeLeft() > 0);
        }

        if (to instanceof ItemWielder) {
            ((ItemWielder)to).updateItemUsage(from.getActiveHand(), from.getActiveItem(), from.getItemUseTimeLeft());
        }

        if (from.age < 100 || from instanceof PlayerEntity && (((PlayerEntity)from).isCreative() || ((PlayerEntity)from).isSpectator())) {
            to.extinguish();
        }

        if (to.isOnFire()) {
            from.setOnFireFor(1);
        } else {
            from.extinguish();
        }

        to.setSneaking(from.isSneaking());
        if (to instanceof PlayerEntity) {
            to.setPose(from.getPose());
        }
    }

    protected void copyInventory(LivingEntity from, LivingEntity l) {
        for (EquipmentSlot i : EquipmentSlot.values()) {
            if (!skipSlot(i)) {
                ItemStack neu = from.getEquippedStack(i);
                ItemStack old = l.getEquippedStack(i);
                if (old != neu) {
                    l.equipStack(i, neu);
                }
            }
        }
    }

    protected boolean skipSlot(EquipmentSlot slot) {
        return false;
    }

    protected boolean isSneakingOnGround(Caster<?> source) {
        Entity e = source.getEntity();
        return e.isSneaking() && (e.isOnGround() && !(e instanceof PlayerEntity && ((PlayerEntity)e).getAbilities().flying));
    }

    public static <T extends Entity> void register(Supplier<EntityBehaviour<T>> behaviour, EntityType<?>... types) {
        for (EntityType<?> type : types) {
            Registry.register(REGISTRY, EntityType.getId(type), behaviour.get());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityBehaviour<T> forEntity(@Nullable T entity) {
        if (entity == null) {
            return (EntityBehaviour<T>)DEFAULT;
        }
        return (EntityBehaviour<T>)REGISTRY.getOrEmpty(EntityType.getId(entity.getType())).orElse(DEFAULT);
    }

    static {
        register(PlayerBehaviour::new, EntityType.PLAYER);
        register(FallingBlockBehaviour::new, EntityType.FALLING_BLOCK);
        register(MobBehaviour::new, EntityType.RAVAGER, EntityType.IRON_GOLEM);
        register(HoppingBehaviour::new, EntityType.RABBIT, EntityType.SLIME, EntityType.MAGMA_CUBE);
        register(TraderBehaviour::new, EntityType.VILLAGER, EntityType.WANDERING_TRADER);
        register(SteedBehaviour::new, EntityType.HORSE, EntityType.DONKEY, EntityType.SKELETON_HORSE, EntityType.ZOMBIE_HORSE);
        register(SheepBehaviour::new, EntityType.SHEEP);
        register(BeeBehaviour::new, EntityType.BEE);
        register(GhastBehaviour::new, EntityType.GHAST);
        register(AxolotlBehaviour::new, EntityType.AXOLOTL);
        register(EndermanBehaviour::new, EntityType.ENDERMAN);
        EntityBehaviour.<LlamaEntity>register(() -> new RangedAttackBehaviour<>(SoundEvents.ENTITY_LLAMA_SPIT, LlamaSpitEntity::new), EntityType.LLAMA, EntityType.TRADER_LLAMA);
        EntityBehaviour.<SnowGolemEntity>register(() -> new RangedAttackBehaviour<>(SoundEvents.ENTITY_SNOW_GOLEM_SHOOT, SnowballEntity::new), EntityType.SNOW_GOLEM);
        register(SpellcastingIllagerBehaviour::new, EntityType.ILLUSIONER, EntityType.EVOKER);
        register(ShulkerBehaviour::new, EntityType.SHULKER);
        register(CreeperBehaviour::new, EntityType.CREEPER);
        register(SilverfishBehaviour::new, EntityType.SILVERFISH);
        register(ChickenBehaviour::new, EntityType.CHICKEN);
        register(BlazeBehaviour::new, EntityType.BLAZE);
        register(MinecartBehaviour::new, EntityType.CHEST_MINECART, EntityType.COMMAND_BLOCK_MINECART, EntityType.FURNACE_MINECART, EntityType.HOPPER_MINECART, EntityType.MINECART, EntityType.SPAWNER_MINECART, EntityType.TNT_MINECART);
    }
}
