package come.minelittlepony.unicopia.forgebullshit;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class FBS {

    public static void init() {
        CapabilityManager.INSTANCE.register(IPlayerCapabilitiesProxyContainer.class,
                new Storage(), DefaultPlayerCapabilitiesProxyContainer::new);
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(new ResourceLocation("unicopia", "race"), new Provider((EntityPlayer)event.getObject()));
        }
    }

    public static void clone(PlayerEvent.Clone event) {
        final IPlayerCapabilitiesProxyContainer original = of(event.getOriginal());

        if (original == null) {
            return;
        }

        final IPlayerCapabilitiesProxyContainer clone = of(event.getEntity());

        clone.getPlayer().copyFrom(original.getPlayer());
    }

    public static IPlayerCapabilitiesProxyContainer of(Entity entity) {
        if (entity.hasCapability(DefaultPlayerCapabilitiesProxyContainer.CAPABILITY, EnumFacing.DOWN)) {
            return entity.getCapability(DefaultPlayerCapabilitiesProxyContainer.CAPABILITY, EnumFacing.DOWN).withEntity((EntityPlayer)entity);
        }

        return null;
    }
}
