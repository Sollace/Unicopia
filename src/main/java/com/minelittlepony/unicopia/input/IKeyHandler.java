package com.minelittlepony.unicopia.input;

public interface IKeyHandler {

    void addKeybind(IKeyBind bind);

    default void onKeyInput() {

    }
}
