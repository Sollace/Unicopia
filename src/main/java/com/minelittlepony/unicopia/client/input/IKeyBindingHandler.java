package com.minelittlepony.unicopia.client.input;

public interface IKeyBindingHandler {

    void addKeybind(IKeyBinding bind);

    default void onKeyInput() {

    }

    public interface IKeyBinding {

        String getKeyCategory();

        String getKeyName();

        int getKeyCode();

    }
}
