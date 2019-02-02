package com.minelittlepony.unicopia.forgebullshit;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Wraps the forge Capabilities API into an easier to manage, simple interface.
 */
@FUF(reason = "Capabilities API is such a mess. I'm not going to rewrite it all just to add capabilities to more stuff.")
@EventBusSubscriber(modid = Unicopia.MODID)
public class FBS {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void init() {
        CapabilityManager.INSTANCE.register(ICapabilitiesProxyContainer.class,
                new Storage(), DefaultEntityCapabilitiesProxyContainer::new);
    }

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer
         || event.getObject() instanceof EntityItem) {
            event.addCapability(new ResourceLocation("unicopia", "race"), new Provider(event.getObject()));
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        final ICapabilitiesProxyContainer<EntityPlayer> original = of(event.getOriginal());

        if (original == null) {
            return;
        }

        final ICapabilitiesProxyContainer<EntityPlayer> clone = of((EntityPlayer)event.getEntity());

        clone.getPlayer().copyFrom(original.getPlayer());
    }

    @SubscribeEvent
    public static void onDimensionalTravel(EntityTravelToDimensionEvent event) {
        final ICapabilitiesProxyContainer<?> original = of(event.getEntity());

        if (original == null) {
            return;
        }

        original.getRaceContainer().onDimensionalTravel(event.getDimension());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> ICapabilitiesProxyContainer<T> of(T entity) {
        if (entity.hasCapability(DefaultEntityCapabilitiesProxyContainer.CAPABILITY, EnumFacing.DOWN)) {
            return ((ICapabilitiesProxyContainer<T>)entity
                    .getCapability(DefaultEntityCapabilitiesProxyContainer.CAPABILITY, EnumFacing.DOWN))
                    .withEntity(entity);
        }

        return null;
    }
}
