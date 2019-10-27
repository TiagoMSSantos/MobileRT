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
        viewText_ = viewText;
        requestRender_ = requestRender;
        finishRender_ = finishRender;
        period_ = period;
        timer_ = () -> {
            viewText_.FPS();
            viewText_.fpsT_ = String.format(Locale.US, "fps:%.1f", viewText_.getFPS());
            viewText_.fpsRenderT_ = String.format(Locale.US, "[%.1f]", viewText_.fps_);
            final long timeRenderer = viewText_.getTimeRenderer();
            viewText_.timeFrameT_ = String.format(Locale.US, ",t:%.2fs", timeRenderer / 1000.0f);
            final long currentTime = SystemClock.elapsedRealtime();
            viewText_.timeT_ = String.format(Locale.US, "[%.2fs]", (currentTime - viewText_.start_) / 1000.0f);
            viewText_.allocatedT_ = ",m:" + Debug.getNativeHeapAllocatedSize() / 1048576L + "mb";
            viewText_.sampleT_ = "," + viewText_.getSample();
            final int stage = viewText_.isWorking();
            viewText_.stageT_ = DrawView.Stage.values()[stage].toString();
            requestRender.run();
            publishProgress();
            if (stage != DrawView.Stage.busy.id_) {
                scheduler_.shutdown();
            }
        };
    }

    @Override
    protected final Void doInBackground(final Void... params) {
        scheduler_.scheduleAtFixedRate(timer_, 0L, period_, TimeUnit.MILLISECONDS);
        boolean running = true;
        do {
            try {
                running = !scheduler_.awaitTermination(1, TimeUnit.DAYS);
            } catch (final InterruptedException ex) {
                Log.e("InterruptedException", Objects.requireNonNull(ex.getMessage()));
                System.exit(1);
            }
        } while (running);
        return null;
    }

    @Override
    protected final void onProgressUpdate(final Void... progress) {
        viewText_.printText();
    }

    @Override
    protected final void onPostExecute(final Void result) {
        viewText_.printText();
        viewText_.buttonRender_.setText(R.string.render);
        requestRender_.run();
        finishRender_.run();
    }
}
