package puscas.mobilertapp;

final class TouchTracker {
    final int pointerID_;
    final int primitiveID_;
    float x_;
    float y_;

    TouchTracker(final int pointerID, final int primitiveID, final float x, final float y) {
        super();
        this.pointerID_ = pointerID;
        this.primitiveID_ = primitiveID;
        this.x_ = x;
        this.y_ = y;
    }
}
