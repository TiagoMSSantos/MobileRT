package puscas.mobilertapp;

/**
 * The states which the Ray Tracer engine can have.
 */
enum State {

    /**
     * The {@link State} for the Ray Tracer engine when is IDLE.
     */
    IDLE(0),

    /**
     * The {@link State} for the Ray Tracer engine when is BUSY.
     */
    BUSY(1),

    /**
     * The {@link State} for the Ray Tracer engine when it ended rendering.
     */
    END(2),

    /**
     * The {@link State} for the Ray Tracer engine when was stopped.
     */
    STOP(3);

    /**
     * @see State#getId()
     */
    private final int id;

    /**
     * The constructor for this {@link Enum}.
     *
     * @param id The current state of the Ray Tracer engine.
     */
    State(final int id) {
        this.id = id;
    }

    /**
     * Gets the current State of the Ray Tracer engine.
     */
    int getId() {
        return this.id;
    }
}
