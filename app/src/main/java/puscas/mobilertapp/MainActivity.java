package puscas.mobilertapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import static puscas.mobilertapp.ConstantsError.CANT_GET_NUMBER_OF_CORES_AVAILABLE;
import static puscas.mobilertapp.ConstantsMethods.ON_DESTROY;
import static puscas.mobilertapp.ConstantsMethods.ON_DETACHED_FROM_WINDOW;
import static puscas.mobilertapp.ConstantsMethods.START_RENDER;
import static puscas.mobilertapp.ConstantsRenderer.REQUIRED_OPENGL_VERSION;
import static puscas.mobilertapp.ConstantsToast.PLEASE_INSTALL_FILE_MANAGER;
import static puscas.mobilertapp.ConstantsUI.CHECK_BOX_RASTERIZE;
import static puscas.mobilertapp.ConstantsUI.PICKER_ACCELERATOR;
import static puscas.mobilertapp.ConstantsUI.PICKER_SAMPLES_LIGHT;
import static puscas.mobilertapp.ConstantsUI.PICKER_SAMPLES_PIXEL;
import static puscas.mobilertapp.ConstantsUI.PICKER_SCENE;
import static puscas.mobilertapp.ConstantsUI.PICKER_SHADER;
import static puscas.mobilertapp.ConstantsUI.PICKER_SIZES;
import static puscas.mobilertapp.ConstantsUI.PICKER_THREADS;
import static puscas.mobilertapp.ConstantsUI.SDCARD_EMULATOR_NAME;

/**
 * The main {@link Activity} for the Android User Interface.
 */
public final class MainActivity extends Activity {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    static {
        try {
            System.loadLibrary("MobileRT");
            System.loadLibrary("Components");
            System.loadLibrary("AppInterface");
        } catch (final RuntimeException ex) {
            LOGGER.severe("WARNING: Could not load native library: " + ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * The request code for the new {@link Activity} to open an OBJ file.
     */
    private static final int OPEN_FILE_REQUEST_CODE = 1;

    /**
     * The custom {@link GLSurfaceView} for displaying OpenGL rendering.
     */
    private DrawView drawView = null;

    /**
     * The {@link NumberPicker} to select the scene to render.
     */
    private NumberPicker pickerScene = null;

    /**
     * The {@link NumberPicker} to select the shader.
     */
    private NumberPicker pickerShader = null;

    /**
     * The {@link NumberPicker} to select the number of threads.
     */
    private NumberPicker pickerThreads = null;

    /**
     * The {@link NumberPicker} to select the accelerator.
     */
    private NumberPicker pickerAccelerator = null;

    /**
     * The {@link NumberPicker} to select the number of samples per pixel.
     */
    private NumberPicker pickerSamplesPixel = null;

    /**
     * The {@link NumberPicker} to select the number of samples per light.
     */
    private NumberPicker pickerSamplesLight = null;

    /**
     * The {@link NumberPicker} to select the desired resolution for the rendered image.
     */
    private NumberPicker pickerResolutions = null;

    /**
     * The {@link CheckBox} to select whether should render a preview of the scene (rasterize) or not.
     */
    private CheckBox checkBoxRasterize = null;

    /**
     * The path to a directory containing the OBJ and MTL files of a scene.
     */
    private String sceneFilePath = null;

    /**
     * Auxiliary method to readjust the width and height of the image by rounding down the value to a multiple of the
     * number of blocks in the Ray Tracer engine.
     */
    private native int RTResize(final int size);

    /**
     * Helper method that gets the number of CPU cores in the Android device for devices with the SDK API version < 17.
     *
     * @return The number of CPU cores.
     */
    private static int getNumCoresOldPhones() {
        int numCores = 0;
        try {
            final File dir = new File("/sys/devices/system/cpu/");
            final File[] files = dir.listFiles((pathname) -> Pattern.matches("cpu[0-9]+", pathname.getName()));
            Preconditions.checkNotNull(Objects.requireNonNull(files), "");

            numCores = files.length;
        } catch (final RuntimeException ex) {
            LOGGER.severe(CANT_GET_NUMBER_OF_CORES_AVAILABLE);
            System.exit(1);
        }
        return numCores;
    }

    /**
     * Helper method which gets the number of CPU cores.
     *
     * @return The number of CPU cores.
     */
    private static int getNumOfCores() {
        return (Build.VERSION.SDK_INT < 17)
                ? MainActivity.getNumCoresOldPhones()
                : Runtime.getRuntime().availableProcessors();
    }

    /**
     * Helper method which starts or stops the rendering process.
     *
     * @param scenePath The path to a directory containing the OBJ and MTL files of a scene to render.
     */
    private void startRender(@NonNull final String scenePath) {
        final int scene = this.pickerScene.getValue();
        final int shader = this.pickerShader.getValue();
        final int threads = this.pickerThreads.getValue();
        final int accelerator = this.pickerAccelerator.getValue();
        final int samplesPixel = Integer.parseInt(this.pickerSamplesPixel.getDisplayedValues()
                [this.pickerSamplesPixel.getValue() - 1]);
        final int samplesLight = Integer.parseInt(this.pickerSamplesLight.getDisplayedValues()
                [this.pickerSamplesLight.getValue() - 1]);
        final String strResolution = this.pickerResolutions.getDisplayedValues()[this.pickerResolutions.getValue() - 1];
        final int width = Integer.parseInt(strResolution.substring(0, strResolution.indexOf('x')));
        final int height = Integer.parseInt(strResolution.substring(strResolution.indexOf('x') + 1));
        final String objFilePath = scenePath + ".obj";
        final String mtlFilePath = scenePath + ".mtl";
        final String camFilePath = scenePath + ".cam";
        final boolean rasterize = this.checkBoxRasterize.isChecked();

        this.drawView.renderScene(
                scene,
                shader,
                threads,
                accelerator,
                samplesPixel,
                samplesLight,
                width,
                height,
                objFilePath,
                mtlFilePath,
                camFilePath,
                rasterize
        );
    }

    /**
     * Helper method which calls a new {@link Activity} with a file manager to select the OBJ file for the Ray
     * Tracer engine.
     */
    private void showFileChooser() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            final Intent intentChooseFile = Intent.createChooser(intent, "Select a File to Upload");
            startActivityForResult(intentChooseFile, OPEN_FILE_REQUEST_CODE);
        } catch (final android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, PLEASE_INSTALL_FILE_MANAGER, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Helper method which asks the user for permission to read the external SD card if it doesn't have yet.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN) private void checksStoragePermission() {
        final int permissionStorageCode = 1;
        final int permissionCheckRead = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        );
        if (permissionCheckRead != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, permissions, permissionStorageCode);
        }
    }

    /**
     * Helper method which reads a text based asset file.
     *
     * @param filePath The path to the file (relative to the asset directory).
     * @return A {@link String} containing the contents of the asset file.
     */
    private String readTextAsset(final String filePath) {
        final AssetManager assetManager = getAssets();
        String asset = null;
        try (final InputStream stream = assetManager.open(filePath)) {
            final int size = stream.available();
            final byte[] buffer = new byte[size];
            final int bytes = stream.read(buffer);
            if (bytes > 0) {
                asset = new String(buffer);
            }
        } catch (final OutOfMemoryError ex1) {
            LOGGER.severe("Not enough memory for asset  " + filePath);
            LOGGER.severe(Objects.requireNonNull(ex1.getMessage()));
            throw ex1;
        } catch (final IOException ex2) {
            LOGGER.severe("Couldn't read asset " + filePath);
            LOGGER.severe(Objects.requireNonNull(ex2.getMessage()));
            System.exit(1);
        }
        return asset;
    }

    /**
     * Helper method which checks if the Android device has support for OpenGL ES 2.0.
     *
     * @return {@code True} if the device has support for OpenGL ES 2.0 or {@code False} otherwise.
     */
    private static boolean checkGL20Support() {
        final EGL10 egl = (EGL10) EGLContext.getEGL();
        final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        final int[] version = new int[2];
        egl.eglInitialize(display, version);

        final int[] configAttribs = {
                EGL10.EGL_RED_SIZE, 4,
                EGL10.EGL_GREEN_SIZE, 4,
                EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_NONE
        };

        final EGLConfig[] configs = new EGLConfig[10];
        final int[] numConfig = new int[1];
        egl.eglChooseConfig(display, configAttribs, configs, 10, numConfig);
        egl.eglTerminate(display);
        return numConfig[0] > 0;
    }

    /**
     * Starts the rendering process when the user clicks the render {@link Button}.
     *
     * @param view The view of the {@link Activity}.
     */
    public void startRender(@NonNull final View view) {
        LOGGER.info(START_RENDER);

        this.sceneFilePath = "";
        final Scene scene = Scene.values()[this.pickerScene.getValue()];
        final MainRenderer renderer = this.drawView.getRenderer();
        final State state = renderer.getState();
        if (state == State.BUSY) {
            this.drawView.stopDrawing();
        } else {
            switch (scene) {
                case OBJ:
                    showFileChooser();
                    break;

                case TEST:
//                    final String scenePath = "conference/conference";
//                    final String scenePath = "teapot/teapot";
                    final String scenePath = "CornellBox/CornellBox-Water";
//                    final String scenePath = "buddha/buddha";
//                    final String scenePath = "powerplant/powerplant";
//                    final String scenePath = "San_Miguel/san-miguel";

                    String sdCardPath = Environment.getExternalStorageDirectory().getPath();

                    // Fix Android device path to an external SD card.
                    sdCardPath = sdCardPath.replace("/storage/sdcard0", "/storage/extSdCard");
                    // Fix Android emulator path to an external SD card.
                    sdCardPath = sdCardPath.replace("/emulated/0", "/" + SDCARD_EMULATOR_NAME);

                    this.sceneFilePath = sdCardPath + "/WavefrontOBJs/" + scenePath;
                    startRender(this.sceneFilePath);
                    break;

                default:
                    startRender(this.sceneFilePath);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected final void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int defaultPickerScene = 0;
        int defaultPickerShader = 0;
        int defaultPickerThreads = 1;
        int defaultPickerAccelerator = 1;
        int defaultPickerSamplesPixel = 1;
        int defaultPickerSamplesLight = 1;
        int defaultPickerSizes = 4;
        boolean defaultCheckBoxRasterize = true;
        if (savedInstanceState != null) {
            defaultPickerScene = savedInstanceState.getInt(PICKER_SCENE);
            defaultPickerShader = savedInstanceState.getInt(PICKER_SHADER);
            defaultPickerThreads = savedInstanceState.getInt(PICKER_THREADS);
            defaultPickerAccelerator = savedInstanceState.getInt(PICKER_ACCELERATOR);
            defaultPickerSamplesPixel = savedInstanceState.getInt(PICKER_SAMPLES_PIXEL);
            defaultPickerSamplesLight = savedInstanceState.getInt(PICKER_SAMPLES_LIGHT);
            defaultPickerSizes = savedInstanceState.getInt(PICKER_SIZES);
            defaultCheckBoxRasterize = savedInstanceState.getBoolean(CHECK_BOX_RASTERIZE);
        }

        try {
            setContentView(R.layout.activity_main);
        } catch (final RuntimeException ex) {
            LOGGER.severe(Objects.requireNonNull(ex.getMessage()));
            System.exit(1);
        }

        this.drawView = this.findViewById(R.id.drawLayout);
        this.pickerScene = this.findViewById(R.id.pickerScene);
        this.pickerShader = this.findViewById(R.id.pickerShader);
        this.pickerSamplesPixel = this.findViewById(R.id.pickerSamplesPixel);
        this.pickerSamplesLight = this.findViewById(R.id.pickerSamplesLight);
        this.pickerAccelerator = this.findViewById(R.id.pickerAccelerator);
        this.pickerThreads = this.findViewById(R.id.pickerThreads);
        this.pickerResolutions = this.findViewById(R.id.pickerSize);
        final TextView textView = this.findViewById(R.id.timeText);
        final Button renderButton = this.findViewById(R.id.renderButton);
        final ActivityManager assetManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        Preconditions.checkNotNull(this.pickerResolutions);
        Preconditions.checkNotNull(this.pickerThreads);
        Preconditions.checkNotNull(this.pickerAccelerator);
        Preconditions.checkNotNull(this.pickerSamplesLight);
        Preconditions.checkNotNull(this.pickerSamplesPixel);
        Preconditions.checkNotNull(this.pickerShader);
        Preconditions.checkNotNull(this.pickerScene);
        Preconditions.checkNotNull(this.drawView);
        Preconditions.checkNotNull(textView);
        Preconditions.checkNotNull(renderButton);
        Preconditions.checkNotNull(Objects.requireNonNull(assetManager));

        final ConfigurationInfo configurationInfo = assetManager.getDeviceConfigurationInfo();
        final boolean supportES2 = (configurationInfo.reqGlEsVersion >= REQUIRED_OPENGL_VERSION);

        if (supportES2 && MainActivity.checkGL20Support()) {
            this.drawView.setVisibility(View.INVISIBLE);
            this.drawView.setEGLContextClientVersion(2);
            this.drawView.setEGLConfigChooser(8, 8, 8, 8, 24, 0);

            final MainRenderer renderer = this.drawView.getRenderer();
            final Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.DKGRAY);
            renderer.setBitmap(bitmap);
            final String vertexShader = this.readTextAsset("Shaders/VertexShader.glsl");
            final String fragmentShader = this.readTextAsset("Shaders/FragmentShader.glsl");
            final String vertexShaderRaster = this.readTextAsset("Shaders/VertexShaderRaster.glsl");
            final String fragmentShaderRaster = this.readTextAsset("Shaders/FragmentShaderRaster.glsl");
            renderer.setVertexShaderCode(vertexShader);
            renderer.setFragmentShaderCode(fragmentShader);
            renderer.setVertexShaderCodeRaster(vertexShaderRaster);
            renderer.setFragmentShaderCodeRaster(fragmentShaderRaster);

            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            this.drawView.setViewAndActivityManager(textView, activityManager);
            this.drawView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            this.drawView.setVisibility(View.VISIBLE);

            renderButton.setOnLongClickListener((final View view) -> {
                this.recreate();
                return false;
            });
            renderer.setButtonRender(renderButton);

            this.drawView.setPreserveEGLContextOnPause(true);
        } else {
            LOGGER.severe("Your device doesn't support ES 2. (" + configurationInfo.reqGlEsVersion + ')');
            System.exit(1);
        }

        final String[] scenes = Scene.getNames();
        this.pickerScene.setMinValue(0);
        this.pickerScene.setMaxValue(scenes.length - 1);
        this.pickerScene.setWrapSelectorWheel(true);
        this.pickerScene.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerScene.setValue(defaultPickerScene);
        this.pickerScene.setDisplayedValues(scenes);

        final String[] shaders = Shader.getNames();
        this.pickerShader.setMinValue(0);
        this.pickerShader.setMaxValue(shaders.length - 1);
        this.pickerShader.setWrapSelectorWheel(true);
        this.pickerShader.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerShader.setValue(defaultPickerShader);
        this.pickerShader.setDisplayedValues(shaders);

        final int maxSamplesPixel = 10;
        final String[] samplesPixel = new String[maxSamplesPixel];
        for (int i = 0; i < maxSamplesPixel; i++) {
            samplesPixel[i] = Integer.toString((i + 1) * (i + 1));
        }
        this.pickerSamplesPixel.setMinValue(1);
        this.pickerSamplesPixel.setMaxValue(maxSamplesPixel);
        this.pickerSamplesPixel.setWrapSelectorWheel(true);
        this.pickerSamplesPixel.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerSamplesPixel.setValue(defaultPickerSamplesPixel);
        this.pickerSamplesPixel.setDisplayedValues(samplesPixel);

        final int maxSamplesLight = 100;
        final String[] samplesLight;
        try {
            samplesLight = new String[maxSamplesLight];
        } catch (final OutOfMemoryError ex) {
            LOGGER.severe(ex.getMessage());
            throw ex;
        }
        for (int i = 0; i < maxSamplesLight; i++) {
            samplesLight[i] = Integer.toString(i + 1);
        }
        this.pickerSamplesLight.setMinValue(1);
        this.pickerSamplesLight.setMaxValue(maxSamplesLight);
        this.pickerSamplesLight.setWrapSelectorWheel(true);
        this.pickerSamplesLight.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerSamplesLight.setValue(defaultPickerSamplesLight);
        this.pickerSamplesLight.setDisplayedValues(samplesLight);

        final String[] accelerators = Accelerator.getNames();
        this.pickerAccelerator.setMinValue(0);
        this.pickerAccelerator.setMaxValue(accelerators.length - 1);
        this.pickerAccelerator.setWrapSelectorWheel(true);
        this.pickerAccelerator.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerAccelerator.setValue(defaultPickerAccelerator);
        this.pickerAccelerator.setDisplayedValues(accelerators);

        final int maxCores = MainActivity.getNumOfCores();
        this.pickerThreads.setMinValue(1);
        this.pickerThreads.setMaxValue(maxCores);
        this.pickerThreads.setWrapSelectorWheel(true);
        this.pickerThreads.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerThreads.setValue(defaultPickerThreads);

        final int maxSizes = 9;
        final String[] resolutions;
        try {
            resolutions = new String[maxSizes];
        } catch (final OutOfMemoryError ex) {
            LOGGER.severe(ex.getMessage());
            throw ex;
        }
        resolutions[0] = String.format(Locale.US, "%.2f", 0.05F) + 'x';
        for (int i = 2; i < maxSizes; i++) {
            final float value = ((float) i + 1.0F) * 0.1F;
            resolutions[i - 1] = String.format(Locale.US, "%.2f", value * value) + 'x';
        }
        resolutions[maxSizes - 1] = String.format(Locale.US, "%.2f", 1.0F) + 'x';
        this.pickerResolutions.setMinValue(1);
        this.pickerResolutions.setMaxValue(maxSizes);
        this.pickerResolutions.setWrapSelectorWheel(true);
        this.pickerResolutions.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerResolutions.setValue(defaultPickerSizes);

        this.checkBoxRasterize = this.findViewById(R.id.preview);
        this.checkBoxRasterize.setChecked(defaultCheckBoxRasterize);
        final int scale = Math.round(getResources().getDisplayMetrics().density);
        this.checkBoxRasterize.setPadding(
            this.checkBoxRasterize.getPaddingLeft() - (5 * scale),
            this.checkBoxRasterize.getPaddingTop(),
            this.checkBoxRasterize.getPaddingRight(),
            this.checkBoxRasterize.getPaddingBottom()
        );

        final ViewTreeObserver vto = this.drawView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {
            final float widthView = (float) this.drawView.getWidth();
            final float heightView = (float) this.drawView.getHeight();

            float size = 0.05F;
            int width = RTResize(Math.round(widthView * size));
            int height = RTResize(Math.round(heightView * size));
            resolutions[0] = Integer.toString(width)+ 'x' + height;

            for (int i = 2; i < maxSizes; i++) {
                size = ((float) i + 1.0F) * 0.1F;
                width = RTResize(Math.round(widthView * size * size));
                height = RTResize(Math.round(heightView * size * size));
                resolutions[i - 1] = String.valueOf(width) + 'x' + height;
            }
            final int fullWidth = RTResize(Math.round(widthView));
            final int fullHeight = RTResize(Math.round(heightView));
            resolutions[maxSizes - 1] = String.valueOf(fullWidth) + 'x' + fullHeight;

            this.pickerResolutions.setDisplayedValues(resolutions);
        });

        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                0
        );

        checksStoragePermission();
    }

    @Override
    protected final void onPostResume() {
        super.onPostResume();

        if (this.sceneFilePath != null) {
            startRender(this.sceneFilePath);
        }
    }

    @Override
    protected final void onResume() {
        super.onResume();

        this.drawView.onResume();
    }

    @Override
    protected final void onPause() {
        super.onPause();

        this.drawView.setPreserveEGLContextOnPause(true);
        this.drawView.onPause();
        this.sceneFilePath = null;
    }

    @Override
    protected final void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LOGGER.info("onRestoreInstanceState");

        final int scene = savedInstanceState.getInt(PICKER_SCENE);
        final int shader = savedInstanceState.getInt(PICKER_SHADER);
        final int threads = savedInstanceState.getInt(PICKER_THREADS);
        final int accelerator = savedInstanceState.getInt(PICKER_ACCELERATOR);
        final int samplesPixel = savedInstanceState.getInt(PICKER_SAMPLES_PIXEL);
        final int samplesLight = savedInstanceState.getInt(PICKER_SAMPLES_LIGHT);
        final int sizes = savedInstanceState.getInt(PICKER_SIZES);
        final boolean rasterize = savedInstanceState.getBoolean(CHECK_BOX_RASTERIZE);

        this.pickerScene.setValue(scene);
        this.pickerShader.setValue(shader);
        this.pickerThreads.setValue(threads);
        this.pickerAccelerator.setValue(accelerator);
        this.pickerSamplesPixel.setValue(samplesPixel);
        this.pickerSamplesLight.setValue(samplesLight);
        this.pickerResolutions.setValue(sizes);
        this.checkBoxRasterize.setChecked(rasterize);
    }

    @Override
    protected final void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        LOGGER.info("onSaveInstanceState");

        final int scene = this.pickerScene.getValue();
        final int shader = this.pickerShader.getValue();
        final int threads = this.pickerThreads.getValue();
        final int accelerator = this.pickerAccelerator.getValue();
        final int samplesPixel = this.pickerSamplesPixel.getValue();
        final int samplesLight = this.pickerSamplesLight.getValue();
        final int sizes = this.pickerResolutions.getValue();
        final boolean rasterize = this.checkBoxRasterize.isChecked();

        outState.putInt(PICKER_SCENE, scene);
        outState.putInt(PICKER_SHADER, shader);
        outState.putInt(PICKER_THREADS, threads);
        outState.putInt(PICKER_ACCELERATOR, accelerator);
        outState.putInt(PICKER_SAMPLES_PIXEL, samplesPixel);
        outState.putInt(PICKER_SAMPLES_LIGHT, samplesLight);
        outState.putInt(PICKER_SIZES, sizes);
        outState.putBoolean(CHECK_BOX_RASTERIZE, rasterize);

        final MainRenderer renderer = this.drawView.getRenderer();
        renderer.RTFinishRender();
        renderer.freeArrays();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LOGGER.info("onRequestPermissionsResult");
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LOGGER.info(ON_DETACHED_FROM_WINDOW);

        this.drawView.onDetachedFromWindow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOGGER.info(ON_DESTROY);

        this.drawView.onDetachedFromWindow();
    }

    @Override
    protected final void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == OPEN_FILE_REQUEST_CODE) {
            final Uri uri = data.getData();
            String filePath = uri.getPath();
            if (filePath != null) {
                final String sdCardDir = Environment.getExternalStorageDirectory().getPath();

                // Fix path to SD card.
                filePath = filePath.replace(":", "/");
                filePath = filePath.replace("/document/primary/", sdCardDir);
                filePath = filePath.replace("/document", "/storage");

                final int lastIndex = filePath.lastIndexOf('.');
                // Remove extension of file.
                this.sceneFilePath = filePath.substring(0, lastIndex);
            }
        }
    }
}
