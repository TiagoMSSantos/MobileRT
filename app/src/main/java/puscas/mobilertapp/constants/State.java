package puscas.mobilertapp.constants;

/**
 * The states which the Ray Tracer engine can have.
 */
public enum State {

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
    FINISHED(2),

    /**
     * The {@link State} for the Ray Tracer engine when was stopped.
     */
    STOPPED(3);

    /**
     * The current State of the Ray Tracer engine.
     */
    private final int id;

    /**
     * The constructor.
     *
     * @param id The id.
     */
    State(final int id) {
        this.id = id;
    }

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public int getId() {
        return id;
    }
}
