package puscas.mobilertapp.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * An abstract class which simulates the deprecated [android.os.AsyncTask] from Java.
 *
 * This implementation uses Kotlin coroutines for the asynchronous tasks.
 */
abstract class AsyncTaskCoroutine {
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
        GlobalScope.launch(Dispatchers.Main) {
            onProgressUpdate()
        }
    }

    /**
     * Executes the task. The task returns
     * itself (this) so that the caller can keep a reference to it.
     *
     * Note: this function schedules the task that will execute the [onPreExecute]
     * method on a Kotlin coroutine which runs on the UI thread.
     * It also schedules a second background Kotlin coroutine
     * which executes some background task, which might be something compute
     * intensive that when it finishes, it will then call the [onPostExecute]
     * method on the UI thread.
     */
    fun <T> execute() {
        GlobalScope.launch(Dispatchers.Main) {
            onPreExecute()
            callAsync()
            onPostExecute()
        }
    }

    /**
     * Helper method which calls the [doInBackground] method in a new Kotlin coroutine.
     */
    private suspend fun callAsync() {
        GlobalScope.async(Dispatchers.IO) {
            doInBackground()
        }.await()
    }
}
