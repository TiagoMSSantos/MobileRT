package puscas.mobilertapp.utils

import java.util.logging.Logger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * An abstract class which simulates the deprecated [android.os.AsyncTask] from Java.
 *
 * This implementation uses Kotlin coroutines for the asynchronous tasks.
 */
abstract class AsyncTaskCoroutine {

    /**
     * The [Logger] for this class.
     */
    private val logger = Logger.getLogger(AsyncTaskCoroutine::class.java.name)

    /**
     * The last [Job] that was triggered to be calculated by the Kotlin coroutines.
     */
    private var lastJob: Deferred<Unit>? = null

    /**
     * Runs on the UI thread before [doInBackground].
     * Invoked directly by [executeAsync].
     *
     * @see [onPostExecute]
     * @see [doInBackground]
     */
    protected abstract fun onPreExecute()

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to [executeAsync]
     * by the caller of this task.
     *
     * This will normally run on a background thread. But to better
     * support testing frameworks, it is recommended that this also tolerates
     * direct execution on the foreground thread, as part of the [executeAsync] call.
     *
     * This method can call [publishProgressAsync] to publish updates
     * on the UI thread.
     *
     * @see [onPreExecute]
     * @see [onPostExecute]
     * @see [publishProgressAsync]
     */
    protected abstract fun doInBackground()

    /**
     * Runs on the UI thread after [publishProgressAsync] is invoked.
     * The specified values are the values passed to [publishProgressAsync].
     *
     * @see [publishProgressAsync]
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
    @DelicateCoroutinesApi
    protected fun publishProgressAsync(): Deferred<Unit> {
        return GlobalScope.async(Dispatchers.Main) {
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
    @DelicateCoroutinesApi
    fun executeAsync(): Deferred<Unit> {
        return GlobalScope.async(Dispatchers.Main) {
            onPreExecute()
            lastJob = GlobalScope.async(Dispatchers.IO) {
                doInBackground()
            }
            (lastJob ?: return@async).await()
            onPostExecute()
        }
    }

    /**
     * Waits for the task to finish.
     */
    fun waitToFinish() {
        runBlocking {
            logger.info("waitToFinish")
            lastJob?.await()
            logger.info("waitToFinish finished")
        }
    }

}
