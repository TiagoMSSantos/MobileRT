package puscas.mobilertapp;

import android.os.Debug;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import java.util.logging.Logger;

final class ViewText {
    private static final Logger LOGGER = Logger.getLogger(ViewText.class.getName());

    float fps_ = 0.0f;
    long start_ = 0L;
    TextView textView_ = null;
    Button buttonRender_ = null;
    String stageT_ = null;
    String fpsT_ = null;
    String timeFrameT_ = null;
    String timeT_ = null;
    String fpsRenderT_ = null;
    String allocatedT_ = null;
    String sampleT_ = null;
    String nPrimitivesT_ = null;
    private String threadsT_ = null;
    private int frame_ = 0;
    private float timebase_ = 0.0f;
    private String resolutionT_ = null;
    private String samplesPixelT_ = null;
    private String samplesLightT_ = null;

    ViewText() {
        super();
        this.frame_ = 0;
        this.timebase_ = 0.0f;
        this.fps_ = 0.0f;
    }

    native float getFPS();

    native long getTimeRenderer();

    native int getSample();

    native int getState();

    void FPS() {
        this.frame_++;
        final float time = SystemClock.elapsedRealtime();
        if ((time - this.timebase_) > 1000.0f) {
            this.fps_ = (this.frame_ * 1000.0f) / (time - this.timebase_);
            this.timebase_ = time;
            this.frame_ = 0;
        }
    }

    void resetPrint(final int width, final int height, final int numThreads,
                    final int samplesPixel, final int samplesLight) {
        this.fpsT_ = String.format(Locale.US, "fps:%.2f", 0.0f);
        this.fpsRenderT_ = String.format(Locale.US, "[%.2f]", 0.0f);
        this.timeFrameT_ = String.format(Locale.US, ",t:%.2fs", 0.0f);
        this.timeT_ = String.format(Locale.US, "[%.2fs]", 0.0f);
        this.stageT_ = " " + Stage.values()[0];
        this.allocatedT_ = ",m:" + Debug.getNativeHeapAllocatedSize() / 1048576L + "mb";
        this.resolutionT_ = ",r:" + width + 'x' + height;
        this.threadsT_ = ",t:" + numThreads;
        this.samplesPixelT_ = ",spp:" + samplesPixel;
        this.samplesLightT_ = ",spl:" + samplesLight;
        this.sampleT_ = ",0";
    }

    final void printText() {
        final String aux = this.fpsT_ + this.fpsRenderT_ + this.resolutionT_ + this.threadsT_ + this.samplesPixelT_ +
                this.samplesLightT_ + this.sampleT_ + '\n'
                + this.stageT_ + this.allocatedT_ + this.timeFrameT_ + this.timeT_ + this.nPrimitivesT_;
        this.textView_.setText(aux);
    }
}
