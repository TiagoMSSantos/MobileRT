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
 * The test suite for the {@link Shader}s used in {@link MainActivity}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class ShaderTest extends AbstractTest {

    /**
     * Tests rendering a scene with the No Shadows shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithNoShadows() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.NO_SHADOWS, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the Whitted shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithWhitted() throws TimeoutException {
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

    /**
     * Tests rendering a scene with the Path Tracing shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithPathTracing() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.PATH_TRACING, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the Depth Map shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithDepthMap() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.DEPTH_MAP, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

    /**
     * Tests rendering a scene with the Diffuse shader.
     *
     * @throws TimeoutException If it couldn't render the whole scene in time.
     */
    @Test(timeout = 2L * 60L * 1000L)
    public void testRenderSceneWithDiffuse() throws TimeoutException {
        final int numCores = UtilsContext.getNumOfCores(this.activity);

        // Mock the reply as the external file manager application, to select an OBJ file.
        final String sdCardPath = UtilsContext.getSdCardPath(this.activity);
        final File fileToObj = new File("/file" + sdCardPath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj");
        final Intent resultData = new Intent(Intent.ACTION_GET_CONTENT);
        resultData.setData(Uri.fromFile(fileToObj));
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.DIFFUSE, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

}
