package puscas.mobilertapp;

/**
 * The states which the ray tracing engine can have.
 */
enum Stage {
    idle(0), busy(1), end(2), stop(3);
    private final int id_;

    Stage(final int id) {
        this.id_ = id;
    }

    public int getId_() {
        return this.id_;
    }
}
