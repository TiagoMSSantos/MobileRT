package puscas.mobilertapp.utils;

import org.jetbrains.annotations.Contract;

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
     * @see State#getId()
     */
    private final int id;

    /**
     * The constructor for this {@link Enum}.
     *
     * @param id The current state of the Ray Tracer engine.
     */
    @Contract(pure = true)
    State(final int id) {
        this.id = id;
    }

    /**
     * Gets the current State of the Ray Tracer engine.
     */
    @Contract(pure = true)
    public int getId() {
        return this.id;
    }
}
