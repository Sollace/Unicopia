package com.minelittlepony.unicopia.enchanting;

public enum PageState {
    LOCKED,
    UNREAD,
    READ;

    public boolean isLocked() {
        return this == LOCKED;
    }

    public boolean isUnread() {
        return this == UNREAD;
    }

    public static PageState of(String s) {
        try {
            if (s != null)
                return valueOf(s.toUpperCase());
        } catch (Throwable e) {}

        return PageState.LOCKED;
    }
}
