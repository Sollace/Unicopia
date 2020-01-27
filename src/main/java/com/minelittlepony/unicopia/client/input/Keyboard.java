package com.minelittlepony.unicopia.client.input;

import com.minelittlepony.unicopia.ServerInteractionManager;

public final class Keyboard {
    private static IKeyBindingHandler keyHandler;

    public static IKeyBindingHandler getKeyHandler() {

        if (keyHandler == null) {
            if (ServerInteractionManager.isClientSide()) {
                keyHandler = new KeyBindingsHandler();
            } else {
                keyHandler = bind -> {};
            }
        }

        return keyHandler;
    }
}
