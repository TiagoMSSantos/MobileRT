package puscas.mobilertapp;

import java.util.logging.Logger;

import static puscas.mobilertapp.ConstantsMethods.GET_NAMES;

/**
 * The available acceleration structures for the Ray Tracer engine.
 */
enum Accelerator {

    /**
     * The naive accelerator.
     */
    NAIVE("Naive"),

    /**
     * The regular grid accelerator.
     */
    REG_GRID("RegGrid"),

    /**
     * The bounding volume hierarchy accelerator.
     */
    BVH("BVH"),

    /**
     * No accelerator.
     */
    NONE("None");

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Accelerator.class.getName());

    /**
     * @see Accelerator#getName()
     */
    private final String name;

    /**
     * The constructor for this {@link Enum}.
     *
     * @param name The accelerator for the Ray Tracer engine.
     */
    Accelerator(final String name) {
        this.name = name;
    }

    /**
     * Gets the name of the accelerator for the Ray Tracer engine.
     */
    String getName() {
        return this.name;
    }

    /**
     * Gets the names of all available accelerators.
     */
    static String[] getNames() {
        LOGGER.info(GET_NAMES);

        final Accelerator[] accelerators = values();
        final int lengthAccelerators = accelerators.length;
        final String[] namesAccelerators = new String[lengthAccelerators];

        for (int i = 0; i < lengthAccelerators; i++) {
            namesAccelerators[i] = accelerators[i].getName();
        }

        return namesAccelerators;
    }
}
