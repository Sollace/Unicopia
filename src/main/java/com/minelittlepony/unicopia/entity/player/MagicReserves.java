package com.minelittlepony.unicopia.entity.player;

public interface MagicReserves {

    /**
     * Gets the amount of exertion this player has put toward any given activity.
     * This is simillar to tiredness.
     */
    Bar getExertion();

    /**
     * Gets the amount of excess energy the player has.
     * This is increased by eating sugar.
     */
    Bar getEnergy();

    /**
     * Gets the amount of magical energy the player has.
     * This is increases slowly with time by performing certain actions.
     */
    Bar getMana();

    public interface Bar {

        /**
         * Gets the current value of this bar
         */
        float get();

        /**
         * Gets the previous value from the last tick.
         * Only updated when calling getPrev again.
         */
        float getPrev();

        /**
         * Sets the absolute value
         */
        void set(float value);

        /**
         * Gets the percentage fill of this bar
         */
        default float getPercentFill() {
            return get() / getMax();
        }

        /**
         * Adds a percentage increment to this bar's current value
         */
        default void add(int step) {
            set(get() + (step / getMax()));
        }

        /**
         * Multiplies the current value.
         */
        default void multiply(float scalar) {
            set(get() * scalar);
        }

        /**
         * Get the maximum value this bar is allowed to contain
         */
        default float getMax() {
            return 100F;
        }
    }
}
