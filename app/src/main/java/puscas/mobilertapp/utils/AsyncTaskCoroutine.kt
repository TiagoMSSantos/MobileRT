package puscas.mobilertapp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * An abstract class which simulates an {@link AsyncTask} from Java.
 * This implementation uses Kotlin coroutines for the asynchronous tasks.
 */
abstract class AsyncTaskCoroutine {
    /**
     * Runs on the UI thread before {@link #doInBackground}.
     * Invoked directly by {@link #execute}.
     * The default version does nothing.
     *
     * @see #onPostExecute
     * @see #doInBackground
     */
    protected abstract fun onPreExecute()

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     *
     * This will normally run on a background thread. But to better
     * support testing frameworks, it is recommended that this also tolerates
     * direct execution on the foreground thread, as part of the {@link #execute} call.
     *
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    protected abstract fun doInBackground()

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.
     * To better support testing frameworks, it is recommended that this be
     * written to tolerate direct execution as part of the execute() call.
     * The default version does nothing.</p>
     *
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    protected abstract fun onPostExecute()

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     * The default version does nothing.
     *
     * @see #publishProgress
     * @see #doInBackground
     */
    protected abstract fun onProgressUpdate()

    /**
     * This method can be invoked from {@link #doInBackground} to
     * publish updates on the UI thread while the background computation is
     * still running. Each call to this method will trigger the execution of
     * {@link #onProgressUpdate} on the UI thread.
     *
     * {@link #onProgressUpdate} will not be called if the task has been
     * canceled.
     *
     * @see #onProgressUpdate
     * @see #doInBackground
     */
    protected fun publishProgress() {
        GlobalScope.launch(Dispatchers.Main) {
            onProgressUpdate()
        }
    }

    /**
     * Executes the task with the specified parameters. The task returns
     * itself (this) so that the caller can keep a reference to it.
     *
     * <p>Note: this function schedules the task on a queue for a single background
     * thread or pool of threads depending on the platform version.  When first
     * introduced, AsyncTasks were executed serially on a single background thread.
     * Starting with {@link android.os.Build.VERSION_CODES#DONUT}, this was changed
     * to a pool of threads allowing multiple tasks to operate in parallel. Starting
     * {@link android.os.Build.VERSION_CODES#HONEYCOMB}, tasks are back to being
     * executed on a single thread to avoid common application errors caused
     * by parallel execution.  If you truly want parallel execution, you can use
     * the {@link #executeOnExecutor} version of this method
     * with {@link #THREAD_POOL_EXECUTOR}; however, see commentary there for warnings
     * on its use.
     *
     * <p>This method must be invoked on the UI thread.
     *
     * @see #executeOnExecutor(java.util.concurrent.Executor, Object[])
     * @see #execute(Runnable)
     */
    fun <T> execute() {
        GlobalScope.launch(Dispatchers.Main) {
            onPreExecute()
            callAsync()
            onPostExecute()
        }
    }

    /**
     * Helper method which calls the {@link doInBackground} method in a new Kotlin coroutine.
     */
    private suspend fun callAsync() {
        GlobalScope.async(Dispatchers.IO) {
            doInBackground()
        }.await()
    }
}
