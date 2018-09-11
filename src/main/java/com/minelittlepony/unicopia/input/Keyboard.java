package com.minelittlepony.unicopia.input;

import com.minelittlepony.unicopia.UClient;

public final class Keyboard {
    private static IKeyHandler keyHandler;

    public static IKeyHandler getKeyHandler() {

        if (keyHandler == null) {
            if (UClient.isClientSide()) {
                keyHandler = bind -> {};
            }

            keyHandler = new UKeyHandler();
        }

        return keyHandler;
    }
}
