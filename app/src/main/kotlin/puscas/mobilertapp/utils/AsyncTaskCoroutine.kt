package puscas.mobilertapp.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import java.util.logging.Logger

/**
 * An abstract class which simulates the deprecated [android.os.AsyncTask] from Java.
 *
 * This implementation uses Kotlin coroutines for the asynchronous tasks.
 */
abstract class AsyncTaskCoroutine {

    /**
     * The [Logger] for this class.
     */
    private val LOGGER = Logger.getLogger(AsyncTaskCoroutine::class.java.name)

    /**
     * The last [Job] that was triggered to be calculated by the Kotlin coroutines.
     */
    private var lastJob : Deferred<Unit>? = null;

    /**
     * Runs on the UI thread before [doInBackground].
     * Invoked directly by [execute].
     *
     * @see [onPostExecute]
     * @see [doInBackground]
     */
    protected abstract fun onPreExecute()

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to [execute]
     * by the caller of this task.
     *
     * This will normally run on a background thread. But to better
     * support testing frameworks, it is recommended that this also tolerates
     * direct execution on the foreground thread, as part of the [execute] call.
     *
     * This method can call [publishProgress] to publish updates
     * on the UI thread.
     *
     * @see [onPreExecute]
     * @see [onPostExecute]
     * @see [publishProgress]
     */
    protected abstract fun doInBackground()

    /**
     * Runs on the UI thread after [publishProgress] is invoked.
     * The specified values are the values passed to [publishProgress].
     *
     * @see [publishProgress]
     * @see [doInBackground]
     */
    protected abstract fun onProgressUpdate()

    /**
     * Runs on the UI thread after [doInBackground].
     * To better support testing frameworks, it is recommended that this be
     * written to tolerate direct execution as part of the execute() call.
     *
     * @see [onPreExecute]
     * @see [doInBackground]
     */
    protected abstract fun onPostExecute()

    /**
     * This method can be invoked from [doInBackground] to
     * publish updates on the UI thread while the background computation is
     * still running. Each call to this method will trigger the execution of
     * [onProgressUpdate] on the UI thread.
     *
     * @see [onProgressUpdate]
     * @see [doInBackground]
     */
    protected fun publishProgress() {
        GlobalScope.async(Dispatchers.Main) {
            onProgressUpdate()
        }
    }

    /**
     * Executes the task in a secondary thread.
     *
     * Note: this function schedules the task that will execute the [onPreExecute]
     * method on a Kotlin coroutine which runs on the UI thread.
     * It also schedules a second background Kotlin coroutine
     * which executes some background task, which might be something compute
     * intensive that when it finishes, it will then call the [onPostExecute]
     * method on the UI thread.
     */
    fun execute() {
        GlobalScope.async<Unit>(Dispatchers.Main) {
            onPreExecute()
            lastJob = GlobalScope.async<Unit>(Dispatchers.IO) {
                doInBackground()
            }
            lastJob!!.await()
            onPostExecute()
        }
    }

    /**
     * Waits for the task to finish.
     *
     * Shuts down and waits for the [executorService] to
     * terminate.
     * In the end, resets [executorService] to a new thread
     * pool with [ConstantsRenderer.NUMBER_THREADS] threads.
     */
    fun waitToFinish() {
        runBlocking {
            LOGGER.info("waitToFinish")
            lastJob?.await()
            LOGGER.info("waitToFinish finished")
        }
    }

}
