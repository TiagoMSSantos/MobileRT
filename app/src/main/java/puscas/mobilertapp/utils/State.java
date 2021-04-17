package puscas.mobilertapp.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The states which the Ray Tracer engine can have.
 */
@RequiredArgsConstructor
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
    @Getter
    private final int id;
}
