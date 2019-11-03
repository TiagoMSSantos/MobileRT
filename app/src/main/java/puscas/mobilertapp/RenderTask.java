package puscas.mobilertapp;

import android.os.AsyncTask;
import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

final class RenderTask extends AsyncTask<Void, Void, Void> {
    private final ScheduledExecutorService scheduler_ = Executors.newScheduledThreadPool(1);
    private final ViewText viewText_;
    private final Runnable requestRender_;
    private final Runnable timer_;
    private final Runnable finishRender_;
    private final int period_;

    RenderTask(final ViewText viewText, final Runnable requestRender, final Runnable finishRender, final int period) {
        super();
        this.viewText_ = viewText;
        this.requestRender_ = requestRender;
        this.finishRender_ = finishRender;
        this.period_ = period;
        this.timer_ = () -> {
            this.viewText_.FPS();
            this.viewText_.fpsT_ = String.format(Locale.US, "fps:%.1f", this.viewText_.getFPS());
            this.viewText_.fpsRenderT_ = String.format(Locale.US, "[%.1f]", this.viewText_.fps_);
            final long timeRenderer = this.viewText_.getTimeRenderer();
            this.viewText_.timeFrameT_ = String.format(Locale.US, ",t:%.2fs", timeRenderer / 1000.0f);
            final long currentTime = SystemClock.elapsedRealtime();
            this.viewText_.timeT_ = String.format(Locale.US, "[%.2fs]", (currentTime - this.viewText_.start_) / 1000.0f);
            this.viewText_.allocatedT_ = ",m:" + Debug.getNativeHeapAllocatedSize() / 1048576L + "mb";
            this.viewText_.sampleT_ = "," + this.viewText_.getSample();
            final int stage = this.viewText_.getState();
            this.viewText_.stageT_ = Stage.values()[stage].toString();
            requestRender.run();
            this.publishProgress();
            if (stage != Stage.busy.getId_()) {
                this.scheduler_.shutdown();
            }
        };
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        this.scheduler_.scheduleAtFixedRate(this.timer_, 0L, this.period_, TimeUnit.MILLISECONDS);
        boolean running = true;
        do {
            try {
                running = !this.scheduler_.awaitTermination(1, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                Log.e("InterruptedException", Objects.requireNonNull(ex.getMessage()));
                System.exit(1);
            }
        } while (running);
        return null;
    }

    @Override
    protected final void onProgressUpdate(final Void... values) {
        this.viewText_.printText();
    }

    @Override
    protected final void onPostExecute(final Void result) {
        this.viewText_.printText();
        this.viewText_.buttonRender_.setText(R.string.render);
        this.requestRender_.run();
        this.finishRender_.run();
    }
}
