package puscas.mobilertapp.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.logging.Logger

/**
 * An abstract class which simulates the deprecated [android.os.AsyncTask] from Java.
 *
 * This implementation uses Kotlin coroutines for the asynchronous tasks.
 *
 * @property dispatcherForeground A [Dispatchers] which will execute on Android UI main thread.
 * @property dispatcherBackground A [Dispatchers] which will execute on a background thread.
 */
abstract class AsyncTaskCoroutine protected constructor(
    private val dispatcherForeground: CoroutineDispatcher = Dispatchers.Main,
    private val dispatcherBackground: CoroutineDispatcher = Dispatchers.IO
) {

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
     * Helper method which waits for the [doInBackground] method to finish.
     */
    protected abstract fun waitForTaskToFinish()

    /**
     * Helper method which stops the [AsyncTaskCoroutine].
     */
    protected abstract fun stopTask()

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
    protected fun publishProgressAsync() {
        var job: Deferred<Unit> = GlobalScope.async(context = dispatcherForeground, start = CoroutineStart.DEFAULT, block = {
            onProgressUpdate()
        })
        logger.info("publishProgressAsync: " + job?.isCompleted)
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
    fun executeAsync() {
        var job: Deferred<Unit> = GlobalScope.async(context = dispatcherForeground, start = CoroutineStart.DEFAULT, block = {
            onPreExecute()
            lastJob = GlobalScope.async(context = dispatcherBackground, start = CoroutineStart.DEFAULT, block = {
                doInBackground()
            })
            (lastJob ?: return@async).await()
            onPostExecute()
        })
        logger.info("executeAsync: " + job?.isCompleted)
    }

    /**
     * Waits for the task to finish.
     */
    fun waitToFinish() {
        logger.info("waitToFinish")

        runBlocking(dispatcherBackground) {
            logger.info("waitToFinish 1")
            val finished: Unit? = lastJob?.await()
            logger.info("waitToFinish 2: $finished")
            lastJob?.join()
            logger.info("waitToFinish 3")
            if (finished != null) {
                waitForTaskToFinish()
            } else {
                stopTask()
            }
            logger.info("waitToFinish 4")
        }

        logger.info("waitToFinish finished: " + lastJob?.isCompleted)
    }

}
