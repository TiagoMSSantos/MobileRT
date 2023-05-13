package puscas.mobilertapp.engine;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

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
import puscas.mobilertapp.constants.ConstantsUI;
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
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String internalStoragePath = UtilsContext.getInternalStoragePath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj"));
            final Uri mtlFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl"));
            final Uri camFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")));
        }
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
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String internalStoragePath = UtilsContext.getInternalStoragePath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj"));
            final Uri mtlFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl"));
            final Uri camFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")));
        }
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
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String internalStoragePath = UtilsContext.getInternalStoragePath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj"));
            final Uri mtlFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl"));
            final Uri camFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")));
        }
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
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String internalStoragePath = UtilsContext.getInternalStoragePath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj"));
            final Uri mtlFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl"));
            final Uri camFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")));
        }
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
        final Intent resultData;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            resultData = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            resultData = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        }
        resultData.addCategory(Intent.CATEGORY_OPENABLE);
        resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            resultData.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        final String internalStoragePath = UtilsContext.getInternalStoragePath(this.activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri objFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj"));
            final Uri mtlFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.mtl"));
            final Uri camFile = Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.cam"));
            final ClipData clipData = new ClipData(new ClipDescription("Scene", new String[]{"*" + ConstantsUI.FILE_SEPARATOR + "*"}), new ClipData.Item(objFile));
            clipData.addItem(new ClipData.Item(mtlFile));
            clipData.addItem(new ClipData.Item(camFile));
            resultData.setClipData(clipData);
        } else {
            resultData.setData(Uri.fromFile(new File(internalStoragePath + "/MobileRT/WavefrontOBJs/CornellBox/CornellBox-Water.obj")));
        }
        final Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(IntentMatchers.anyIntent()).respondWith(result);

        assertRenderScene(numCores, Scene.OBJ, Shader.DIFFUSE, Accelerator.BVH, 1, 1, false);
        Intents.intended(IntentMatchers.anyIntent());
    }

}
