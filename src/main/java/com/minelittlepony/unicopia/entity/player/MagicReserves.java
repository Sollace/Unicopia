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
     * Gets the amount of fatigue induced by using magic.
     */
    Bar getExhaustion();

    /**
     * Gets the amount of magical energy the player has.
     * This is increases slowly with time by performing certain actions.
     */
    Bar getMana();

    /**
     * The progress to the next experience level.
     * This is increased slowly by performing actions that consume mana.
     */
    Bar getXp();

    /**
     * Temporary mana charge collected by performing certain tasks.
     */
    Bar getCharge();

    public interface Bar {

        /**
         * Gets the current value of this bar
         */
        float get();

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
         * Gets the shadow fill used for animating on the UI
         */
        float getShadowFill();

        /**
         * Adds a percentage increment to this bar's current value
         */
        default void add(float step) {
            set(get() + (step / getMax()));
        }

        /**
         * Multiplies the current value.
         */
        default void multiply(float scalar) {
            float newVal = get() * scalar;
            set(newVal > -0.0001F && newVal < 0.0001F ? 0 : newVal);
        }

        /**
         * Get the maximum value this bar is allowed to contain
         */
        float getMax();
    }
}
