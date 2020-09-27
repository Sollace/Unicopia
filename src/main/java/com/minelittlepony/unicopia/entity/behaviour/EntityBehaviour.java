package com.minelittlepony.unicopia.entity.behaviour;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.Spell;
import com.minelittlepony.unicopia.util.Registries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class EntityBehaviour<T extends Entity> {

    private static final EntityBehaviour<Entity> DEFAULT = new EntityBehaviour<>();
    private static final Registry<EntityBehaviour<?>> REGISTRY = Registries.createSimple(new Identifier("unicopia", "entity_behaviour"));

    public void update(Caster<?> source, T entity, Spell spell) {

    }

    public T onCreate(T entity, Disguise context) {
        entity.extinguish();
        return entity;
    }

    public void copyBaseAttributes(LivingEntity from, Entity to) {
        copyBaseAttributes(from, to, Vec3d.ZERO);
    }

    public void copyBaseAttributes(LivingEntity from, Entity to, Vec3d positionOffset) {
        // Set first because position calculations rely on it
        to.age = from.age;
        to.removed = from.removed;
        to.setOnGround(from.isOnGround());

        if (Disguise.isAxisAligned(to)) {
            double x = positionOffset.x + Math.floor(from.getX()) + 0.5;
            double y = positionOffset.y + Math.floor(from.getY());
            double z = positionOffset.z + Math.floor(from.getZ()) + 0.5;

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

            to.chunkX = from.chunkX;
            to.chunkY = from.chunkY;
            to.chunkZ = from.chunkZ;

            to.lastRenderX = positionOffset.x + from.lastRenderX;
            to.lastRenderY = positionOffset.y + from.lastRenderY;
            to.lastRenderZ = positionOffset.z + from.lastRenderZ;
        }

        if (to instanceof PlayerEntity) {
            PlayerEntity l = (PlayerEntity)to;

            l.capeX = positionOffset.x + l.getX();
            l.capeY = positionOffset.y + l.getY();
            l.capeZ = positionOffset.z + l.getZ();
        }

        to.setVelocity(from.getVelocity());

        to.pitch = from.pitch;
        to.prevPitch = from.prevPitch;
        to.yaw = from.yaw;
        to.prevYaw = from.prevYaw;
        to.horizontalSpeed = from.horizontalSpeed;
        to.prevHorizontalSpeed = from.prevHorizontalSpeed;

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
            l.setHealth(from.getHealth());

            copyInventory(from, l);
        }

        /*if (to instanceof RangedAttackMob) {
            ItemStack activeItem = from.getActiveItem();

            ((RangedAttackMob)to).setSwingingArms(!activeItem.isEmpty() && activeItem.getUseAction() == UseAction.BOW);
        }*/

        if (to instanceof TameableEntity) {
            ((TameableEntity)to).setSitting(from.isSneaking());
        }

        if (from.age < 100 || from instanceof PlayerEntity && ((PlayerEntity)from).isCreative()) {
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
        register(FallingBlockBehaviour::new, EntityType.FALLING_BLOCK);
        register(VillagerBehaviour::new, EntityType.VILLAGER, EntityType.WANDERING_TRADER);
        register(SheepBehaviour::new, EntityType.SHEEP);
        register(BeeBehaviour::new, EntityType.BEE);
        register(EndermanBehaviour::new, EntityType.ENDERMAN);
        register(SpellcastingIllagerBehaviour::new, EntityType.ILLUSIONER, EntityType.EVOKER);
        register(ShulkerBehaviour::new, EntityType.SHULKER);
        register(CreeperBehaviour::new, EntityType.CREEPER);
        register(SilverfishBehaviour::new, EntityType.SILVERFISH);
        register(ChickenBehaviour::new, EntityType.CHICKEN);
        register(MinecartBehaviour::new, EntityType.CHEST_MINECART, EntityType.COMMAND_BLOCK_MINECART, EntityType.FURNACE_MINECART, EntityType.HOPPER_MINECART, EntityType.MINECART, EntityType.SPAWNER_MINECART, EntityType.TNT_MINECART);
    }
}
