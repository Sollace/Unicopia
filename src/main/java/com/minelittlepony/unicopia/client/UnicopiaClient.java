package com.minelittlepony.unicopia.client;

import com.minelittlepony.unicopia.InteractionManager;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.network.Channel;
import com.minelittlepony.unicopia.network.MsgRequestCapabilities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;

public class UnicopiaClient implements ClientModInitializer {

    private final KeyBindingsHandler keyboard = new KeyBindingsHandler();

    private Race lastPreferredRace = InteractionManager.instance().getPreferredRace();

    @Override
    public void onInitializeClient() {
        lastPreferredRace = InteractionManager.instance().getPreferredRace();
        InteractionManager.INSTANCE = new ClientInteractionManager();

        URenderers.bootstrap();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PlayerEntity player = client.player;

            if (player != null && !player.removed) {
                Race newRace = InteractionManager.instance().getPreferredRace();

                if (newRace != lastPreferredRace) {
                    lastPreferredRace = newRace;

                    Channel.REQUEST_CAPABILITIES.send(new MsgRequestCapabilities(lastPreferredRace));
                }
            }

            keyboard.tick(client);
        });
    }
}
