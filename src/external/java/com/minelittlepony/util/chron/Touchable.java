package com.minelittlepony.util.chron;

//#MineLittlePony#
public abstract class Touchable<T extends Touchable<T>> {

    private long expirationPeriod;

    public boolean hasExpired() {
        return expirationPeriod <= System.currentTimeMillis();
    }

    @SuppressWarnings("unchecked")
    public T touch() {
        expirationPeriod = System.currentTimeMillis() + 30000;
        return (T)this;
    }
}
