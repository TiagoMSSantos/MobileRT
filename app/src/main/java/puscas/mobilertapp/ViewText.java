package puscas.mobilertapp;

import android.os.Debug;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

class ViewText {
    float fps_;
    long start_ = 0;
    int period_ = 0;
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
    private int frame_;
    private float timebase_;
    private String resolutionT_ = null;
    private String samplesPixelT_ = null;
    private String samplesLightT_ = null;

    ViewText() {
        super();
        frame_ = 0;
        timebase_ = 0.0f;
        fps_ = 0.0f;
    }

    native float getFPS();

    native long getTimeRenderer();

    native int getSample();

    native int isWorking();

    void FPS() {
        frame_++;
        final float time = SystemClock.elapsedRealtime();
        if ((time - timebase_) > 1000) {
            fps_ = (frame_ * 1000.0f) / (time - timebase_);
            timebase_ = time;
            frame_ = 0;
        }
    }

    void resetPrint(final int width, final int height, final int numThreads,
                    final int samplesPixel, final int samplesLight) {
        fpsT_ = String.format(Locale.US, "fps:%.2f", 0.0f);
        fpsRenderT_ = String.format(Locale.US, "[%.2f]", 0.0f);
        timeFrameT_ = String.format(Locale.US, ",t:%.2fs", 0.0f);
        timeT_ = String.format(Locale.US, "[%.2fs]", 0.0f);
        stageT_ = " " + DrawView.Stage.values()[0];
        allocatedT_ = ",m:" + Debug.getNativeHeapAllocatedSize() / 1048576L + "mb";
        resolutionT_ = ",r:" + width + 'x' + height;
        threadsT_ = ",t:" + numThreads;
        samplesPixelT_ = ",spp:" + samplesPixel;
        samplesLightT_ = ",spl:" + samplesLight;
        sampleT_ = ",0";
    }

    final void printText() {
        final String aux = fpsT_ + fpsRenderT_ + resolutionT_ + threadsT_ + samplesPixelT_ +
                samplesLightT_ + sampleT_ + '\n'
                + stageT_ + allocatedT_ + timeFrameT_ + timeT_ + nPrimitivesT_;
        textView_.setText(aux);
    }

}
