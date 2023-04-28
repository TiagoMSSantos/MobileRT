package puscas.mobilertapp.engine;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.util.concurrent.TimeoutException;

import puscas.mobilertapp.AbstractTest;
import puscas.mobilertapp.MainActivity;
import puscas.mobilertapp.constants.Accelerator;
import puscas.mobilertapp.constants.Scene;
import puscas.mobilertapp.constants.Shader;
import puscas.mobilertapp.utils.UtilsContext;

/**
 * The test suite for the {@link Accelerator}s used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AcceleratorTest extends AbstractTest {

    /**
     * Tests rendering a scene with the {@link Accelerator#NAIVE} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithNaive() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.NAIVE, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#REG_GRID} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithRegularGrid() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.REG_GRID, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the {@link Accelerator#BVH} accelerator.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithBVH() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.WHITTED, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

}
