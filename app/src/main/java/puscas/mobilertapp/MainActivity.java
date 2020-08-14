package puscas.mobilertapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import java8.util.Optional;
import java8.util.stream.IntStreams;
import java8.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import puscas.mobilertapp.exceptions.FailureException;
import puscas.mobilertapp.utils.Accelerator;
import puscas.mobilertapp.utils.ConstantsMethods;
import puscas.mobilertapp.utils.ConstantsToast;
import puscas.mobilertapp.utils.ConstantsUI;
import puscas.mobilertapp.utils.Scene;
import puscas.mobilertapp.utils.Shader;
import puscas.mobilertapp.utils.State;
import puscas.mobilertapp.utils.Utils;
import puscas.mobilertapp.utils.UtilsContext;
import puscas.mobilertapp.utils.UtilsGL;
import puscas.mobilertapp.utils.UtilsLogging;

/**
 * The main {@link Activity} for the Android User Interface.
 */
public final class MainActivity extends Activity {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());


    /*
     ***********************************************************************
     * Private static fields
     ***********************************************************************
     */

    /**
     * The request code for the new {@link Activity} to open an OBJ file.
     */
    private static final int OPEN_FILE_REQUEST_CODE = 1;

    /**
     * The OpenGL ES version required to run this application.
     */
    private static final int REQUIRED_OPENGL_VERSION = 0x20000;

    /*
     ***********************************************************************
     * Static class initializer
     ***********************************************************************
     */
    static {
        try {
            System.loadLibrary("MobileRT");
            System.loadLibrary("Components");
            System.loadLibrary("AppMobileRT");
        } catch (final RuntimeException ex) {
            throw new FailureException(ex);
        } catch (final UnsatisfiedLinkError ex) {
            UtilsLogging.logThrowable(ex, "MainActivity#static");
        }
    }


    /*
     ***********************************************************************
     * Private instance fields
     ***********************************************************************
     */

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
     * The {@link NumberPicker} to select the desired resolution for the
     * rendered image.
     */
    private NumberPicker pickerResolutions = null;

    /**
     * The {@link CheckBox} to select whether should render a preview of the
     * scene (rasterize) or not.
     */
    private CheckBox checkBoxRasterize = null;

    /**
     * The path to a directory containing the OBJ and MTL files of a scene.
     */
    private String sceneFilePath = null;

    /**
     * Helper method that checks if the supported OpenGL ES version is
     * the version 2 or greater.
     *
     * @param activityManager The {@link ActivityManager} which contains the
     *                        {@link ConfigurationInfo}.
     */
    private static void checksOpenGlVersion(final ActivityManager activityManager) {
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportES2 = (configurationInfo.reqGlEsVersion
            >= REQUIRED_OPENGL_VERSION);

        if (!supportES2 || !UtilsGL.checkGL20Support()) {
            final String msg = "Your device doesn't support ES 2. ("
                + configurationInfo.reqGlEsVersion + ')';
            throw new FailureException(msg);
        }
    }

    /**
     * Initializes a {@link NumberPicker}.
     *
     * @param numberPicker The {@link NumberPicker} to initialize.
     * @param defaultValue The default value to put in the {@link NumberPicker}.
     * @param names        The values to be displayed in the {@link NumberPicker}.
     */
    private static void initializePicker(final NumberPicker numberPicker,
                                         final int defaultValue,
                                         final String[] names) {
        try {
            final int minValue = Integer.parseInt(names[0]);
            numberPicker.setMinValue(minValue);
            numberPicker.setMaxValue(names.length);
        } catch (final NumberFormatException ex) {
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(names.length - 1);
        }

        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        numberPicker.setValue(defaultValue);
        numberPicker.setDisplayedValues(names);
    }

    /*
     ***********************************************************************
     * Overloaded methods
     ***********************************************************************
     */
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");

        try {
            setContentView(R.layout.activity_main);
        } catch (final RuntimeException ex) {
            throw new FailureException(ex);
        }

        initializeViews();

        final TextView textView = findViewById(R.id.timeText);
        final Button renderButton = findViewById(R.id.renderButton);
        final ActivityManager activityManager = (ActivityManager) getSystemService(
            Context.ACTIVITY_SERVICE);

        checksOpenGlVersion(activityManager);
        setupRenderer(textView, renderButton);

        final Optional<Bundle> bundle = Optional.ofNullable(savedInstanceState);
        initializePickers(bundle);
        initializeCheckBoxRasterize(bundle.map(x -> x.getBoolean(ConstantsUI.CHECK_BOX_RASTERIZE))
            .orElse(true));

        checksStoragePermission();
    }

    @Override
    protected void onRestoreInstanceState(@Nonnull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        LOGGER.info("onRestoreInstanceState");

        final int scene = savedInstanceState.getInt(ConstantsUI.PICKER_SCENE);
        final int shader = savedInstanceState.getInt(ConstantsUI.PICKER_SHADER);
        final int threads = savedInstanceState.getInt(ConstantsUI.PICKER_THREADS);
        final int accelerator = savedInstanceState.getInt(ConstantsUI.PICKER_ACCELERATOR);
        final int samplesPixel = savedInstanceState.getInt(ConstantsUI.PICKER_SAMPLES_PIXEL);
        final int samplesLight = savedInstanceState.getInt(ConstantsUI.PICKER_SAMPLES_LIGHT);
        final int sizes = savedInstanceState.getInt(ConstantsUI.PICKER_SIZE);
        final boolean rasterize = savedInstanceState.getBoolean(ConstantsUI.CHECK_BOX_RASTERIZE);

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
    protected void onResume() {
        super.onResume();

        this.drawView.onResume();
        this.drawView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // Start rendering if its resuming from selecting a scene via an
        // external file manager and it was selected a file with a scene to
        // render.
        // This method should be automatically called after `onActivityResult`.
        if (!Strings.isNullOrEmpty(this.sceneFilePath)) {
            startRender(this.sceneFilePath);
        }
    }

    @Override
    protected void onSaveInstanceState(@Nonnull final Bundle outState) {
        super.onSaveInstanceState(outState);
        LOGGER.info("onSaveInstanceState");

        outState.putInt(ConstantsUI.PICKER_SCENE, this.pickerScene.getValue());
        outState.putInt(ConstantsUI.PICKER_SHADER, this.pickerShader.getValue());
        outState.putInt(ConstantsUI.PICKER_THREADS, this.pickerThreads.getValue());
        outState.putInt(ConstantsUI.PICKER_ACCELERATOR, this.pickerAccelerator.getValue());
        outState.putInt(ConstantsUI.PICKER_SAMPLES_PIXEL, this.pickerSamplesPixel.getValue());
        outState.putInt(ConstantsUI.PICKER_SAMPLES_LIGHT, this.pickerSamplesLight.getValue());
        outState.putInt(ConstantsUI.PICKER_SIZE, this.pickerResolutions.getValue());
        outState.putBoolean(ConstantsUI.CHECK_BOX_RASTERIZE, this.checkBoxRasterize.isChecked());

        this.drawView.finishRenderer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LOGGER.info("onPause");

        Utils.handleInterruption("MainActivity#onPause");

        this.drawView.setPreserveEGLContextOnPause(true);
        this.drawView.onPause();
        this.drawView.setVisibility(View.INVISIBLE);
        this.sceneFilePath = null;

        final String message = "onPause" + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOGGER.info(ConstantsMethods.ON_DESTROY);

        this.drawView.onDetachedFromWindow();
        this.drawView.setVisibility(View.INVISIBLE);

        final String message = ConstantsMethods.ON_DESTROY + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LOGGER.info(ConstantsMethods.ON_DETACHED_FROM_WINDOW);

        this.drawView.onDetachedFromWindow();

        final String message = ConstantsMethods.ON_DETACHED_FROM_WINDOW + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }


    /*
     ***********************************************************************
     * Public methods
     ***********************************************************************
     */

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @Nonnull final String[] permissions,
                                           @Nonnull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LOGGER.info("onRequestPermissionsResult");
    }


    /*
     ***********************************************************************
     * Private methods
     ***********************************************************************
     */

    // Ray Tracing methods

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode,
                                    @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == OPEN_FILE_REQUEST_CODE) {
            this.sceneFilePath = Optional.ofNullable(data)
                .map(Intent::getData)
                .map(this::getPathFromFile)
                .orElse("");
        }
    }

    /**
     * Starts the rendering process when the user clicks the render
     * {@link Button}.
     *
     * @param view The view of the {@link Activity}.
     */
    public void startRender(@Nonnull final View view) {
        final String message = String.format(Locale.US, "%s: %s",
            ConstantsMethods.START_RENDER, view.toString());
        LOGGER.info(message);

        final State state = this.drawView.getRayTracerState();
        if (state == State.BUSY) {
            this.drawView.stopDrawing();
        } else {
            startRenderScene();
        }

        final String messageFinished = ConstantsMethods.START_RENDER + ConstantsMethods.FINISHED;
        LOGGER.info(messageFinished);
    }

    /**
     * Helper method which starts or stops the rendering process.
     *
     * @param scenePath The path to a directory containing the OBJ and MTL files
     *                  of a scene to render.
     */
    private void startRender(@Nonnull final String scenePath) {
        LOGGER.info(ConstantsMethods.START_RENDER);

        final Config config = createConfigFromUI(scenePath);
        final int threads = this.pickerThreads.getValue();
        final boolean rasterize = this.checkBoxRasterize.isChecked();

        this.drawView.renderScene(config, threads, rasterize);

        final String message = ConstantsMethods.START_RENDER + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    /**
     * Helper method which starts the rendering process.
     */
    private void startRenderScene() {
        switch (Scene.values()[this.pickerScene.getValue()]) {
            case OBJ:
                callFileManager();
                break;

            case TEST:
                final String scenePath = "CornellBox"
                    + ConstantsUI.FILE_SEPARATOR + "CornellBox-Water";
                final String sdCardPath = UtilsContext.getSdCardPath(this);
                final String lSceneFilePath = sdCardPath + ConstantsUI.FILE_SEPARATOR
                    + "WavefrontOBJs" + ConstantsUI.FILE_SEPARATOR + scenePath;
                startRender(lSceneFilePath);
                break;

            default:
                startRender("");
                break;
        }
    }

    /**
     * Auxiliary method to readjust the width and height of the image by
     * rounding down the value to a multiple of the number of tiles in the
     * Ray Tracer engine.
     *
     * @param size The value to be rounded down to a multiple of the number of
     *             tiles in the Ray Tracer engine.
     * @return The highest value that is smaller than the size passed by
     *     parameter and is a multiple of the number of tiles.
     */
    private native int rtResize(int size);

    /**
     * Gets the path of a file that was loaded with an external file manager.
     * <br/>
     * This method basically translates an {@link Uri} path to a {@link String}
     * but also tries to be compatible with any device / emulator available.
     *
     * @param uri The URI reference for the file.
     * @return The path to the file.
     */
    @Nonnull
    private String getPathFromFile(final Uri uri) {
        final String filePath = StreamSupport.stream(uri.getPathSegments())
            .skip(1L)
            .reduce("",
                (accumulator, segment) -> accumulator + ConstantsUI.FILE_SEPARATOR + segment)
            .replace(ConstantsUI.FILE_SEPARATOR + "sdcard" + ConstantsUI.FILE_SEPARATOR,
                ConstantsUI.FILE_SEPARATOR);

        final int removeIndex = filePath.indexOf(ConstantsUI.PATH_SEPARATOR);
        final String startFilePath = removeIndex >= 0 ? filePath.substring(removeIndex) : filePath;
        final String cleanedFilePath = startFilePath.replace(
            ConstantsUI.PATH_SEPARATOR, ConstantsUI.FILE_SEPARATOR);
        final String filePathWithoutExtension = cleanedFilePath.substring(0,
            cleanedFilePath.lastIndexOf('.'));

        final String sdCardPath = UtilsContext.getSdCardPath(this);
        return sdCardPath + filePathWithoutExtension;
    }

    /**
     * Create a Ray Tracer {@link Config} from the selected {@link NumberPicker}s
     * in the Android UI.
     *
     * @param scenePath The path to the OBJ scene file.
     * @return A {@link Config}.
     */
    @Nonnull
    private Config createConfigFromUI(@Nonnull final String scenePath) {
        final Pair<Integer, Integer> resolution =
            Utils.getResolutionFromPicker(this.pickerResolutions);

        return new Config.Builder()
            .withScene(this.pickerScene.getValue())
            .withShader(this.pickerShader.getValue())
            .withAccelerator(this.pickerAccelerator.getValue())
            .withConfigSamples(
                new ConfigSamples.Builder()
                    .withSamplesPixel(Utils.getValueFromPicker(this.pickerSamplesPixel))
                    .withSamplesLight(Utils.getValueFromPicker(this.pickerSamplesLight))
                    .build()
            )
            .withConfigResolution(
                new ConfigResolution.Builder()
                    .withWidth(resolution.getLeft())
                    .withHeight(resolution.getRight())
                    .build()
            )
            .withObj(scenePath + ".obj")
            .withMaterial(scenePath + ".mtl")
            .withCamera(scenePath + ".cam")
            .build();
    }

    /**
     * Helper method which calls a new {@link Activity} with a file manager to
     * select the OBJ file for the Ray Tracer engine to load.
     */
    private void callFileManager() {
        LOGGER.info("callFileManager");

        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*" + ConstantsUI.FILE_SEPARATOR + "*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            final Intent intentChooseFile = Intent.createChooser(intent,
                "Select an OBJ file to load.");
            startActivityForResult(intentChooseFile, OPEN_FILE_REQUEST_CODE);
        } catch (final android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, ConstantsToast.PLEASE_INSTALL_FILE_MANAGER, Toast.LENGTH_LONG)
                .show();
        }

        final String message = "callFileManager" + ConstantsMethods.FINISHED;
        LOGGER.info(message);
    }

    /**
     * Helper method which asks the user for permission to read the external SD
     * card if it doesn't have yet.
     */
    private void checksStoragePermission() {
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
     * Helper method that initializes all the pickers fields of this
     * {@link Activity}.
     *
     * @param bundle The data state of the {@link Activity}.
     */
    private void initializePickers(final Optional<Bundle> bundle) {
        initializePicker(this.pickerScene, bundle.map(x -> x.getInt(ConstantsUI.PICKER_SCENE))
            .orElse(0), Scene.getNames());
        initializePicker(this.pickerShader, bundle.map(x -> x.getInt(ConstantsUI.PICKER_SHADER))
            .orElse(0), Shader.getNames());
        initializePicker(this.pickerAccelerator,
            bundle.map(x -> x.getInt(ConstantsUI.PICKER_ACCELERATOR))
                .orElse(1), Accelerator.getNames());

        final String[] samplesPixel = IntStreams.range(0, 99)
            .map(value -> (value + 1) * (value + 1))
            .mapToObj(String::valueOf)
            .toArray(String[]::new);
        initializePicker(this.pickerSamplesPixel,
            bundle.map(x -> x.getInt(ConstantsUI.PICKER_SAMPLES_PIXEL))
                .orElse(1), samplesPixel);

        final String[] samplesLight = IntStreams.range(0, 100)
            .map(value -> value + 1)
            .mapToObj(String::valueOf)
            .toArray(String[]::new);
        initializePicker(this.pickerSamplesLight,
            bundle.map(x -> x.getInt(ConstantsUI.PICKER_SAMPLES_LIGHT))
                .orElse(1), samplesLight);

        initializePickerThreads(bundle.map(x -> x.getInt(ConstantsUI.PICKER_THREADS))
            .orElse(1));

        initializeListenersPickers(bundle);
    }

    /**
     * Helper method that sets up some listeners in the {@link View} to initialize
     * some {@link NumberPicker}s, like the resolution picker.
     *
     * @param bundle The data state of the {@link Activity}.
     * @implNote We can only set the resolutions after the views are shown.
     */
    private void initializeListenersPickers(final Optional<Bundle> bundle) {
        final ViewTreeObserver vto = this.drawView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() ->
            initializePickerResolutions(bundle.map(x -> x.getInt(ConstantsUI.PICKER_SIZE))
                .orElse(4), 9));
    }

    /**
     * Helper method that sets up the {@link GLSurfaceView.Renderer} in the
     * {@link DrawView}.
     * This method loads the GLSL shaders and pass them to the
     * {@link GLSurfaceView.Renderer}.
     *
     * @param textView     The {@link TextView} for the {@link GLSurfaceView.Renderer} to pass to
     *                     the {@link AsyncTask} when the Ray Tracing process starts.
     * @param renderButton The {@link TextView} for the {@link GLSurfaceView.Renderer} to
     *                     pass to the {@link AsyncTask} when the Ray Tracing
     *                     process starts.
     */
    private void setupRenderer(final TextView textView, final Button renderButton) {
        this.drawView.setVisibility(View.INVISIBLE);
        this.drawView.setEGLContextClientVersion(2);
        this.drawView.setEGLConfigChooser(8, 8, 8, 8, 3 * 8, 0);

        final ActivityManager activityManager = (ActivityManager) getSystemService(
            Context.ACTIVITY_SERVICE);
        this.drawView.setViewAndActivityManager(textView, activityManager);
        this.drawView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        renderButton.setOnLongClickListener((final View view) -> {
            recreate();
            return false;
        });

        final String shadersPath = ConstantsUI.PATH_SHADERS + ConstantsUI.FILE_SEPARATOR;
        final ImmutableMap<Integer, String> shadersPaths = ImmutableMap.of(
            GLES20.GL_VERTEX_SHADER, shadersPath + "VertexShader.glsl",
            GLES20.GL_FRAGMENT_SHADER, shadersPath + "FragmentShader.glsl");
        final Map<Integer, String> shadersRayTracing =
            UtilsContext.readShaders(this, shadersPaths);

        final ImmutableMap<Integer, String> shadersPreviewPaths = ImmutableMap.of(
            GLES20.GL_VERTEX_SHADER, shadersPath + "VertexShaderRaster.glsl",
            GLES20.GL_FRAGMENT_SHADER, shadersPath + "FragmentShaderRaster.glsl");
        final Map<Integer, String> shadersPreview =
            UtilsContext.readShaders(this, shadersPreviewPaths);

        this.drawView.prepareRenderer(shadersRayTracing, shadersPreview, renderButton);
        this.drawView.setVisibility(View.VISIBLE);
        this.drawView.setPreserveEGLContextOnPause(true);
    }

    /**
     * Initializes the {@link #checkBoxRasterize} field.
     *
     * @param checkBoxRasterize The default value to put in the
     *                          {@link #checkBoxRasterize} field.
     */
    private void initializeCheckBoxRasterize(final boolean checkBoxRasterize) {
        this.checkBoxRasterize.setChecked(checkBoxRasterize);
        final int scale = Math.round(getResources().getDisplayMetrics().density);
        this.checkBoxRasterize.setPadding(
            this.checkBoxRasterize.getPaddingLeft() - (5 * scale),
            this.checkBoxRasterize.getPaddingTop(),
            this.checkBoxRasterize.getPaddingRight(),
            this.checkBoxRasterize.getPaddingBottom()
        );
    }

    /**
     * Initializes the {@link #pickerResolutions} field.
     *
     * @param pickerSizes The default value to put in the
     *                    {@link #pickerResolutions} field.
     * @param maxSizes    The maximum size value for the {@link NumberPicker}.
     */
    private void initializePickerResolutions(final int pickerSizes,
                                             final int maxSizes) {
        this.pickerResolutions.setMinValue(1);
        this.pickerResolutions.setMaxValue(maxSizes - 1);
        this.pickerResolutions.setWrapSelectorWheel(true);
        this.pickerResolutions.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerResolutions.setValue(pickerSizes);

        final double widthView = this.drawView.getWidth();
        final double heightView = this.drawView.getHeight();

        final String[] resolutions = IntStreams.rangeClosed(2, maxSizes)
            .mapToDouble(value -> (double) value)
            .map(value -> (value + 1.0) * 0.1)
            .map(value -> value * value)
            .mapToObj(value -> {
                final int width = rtResize((int) Math.round(widthView * value));
                final int height = rtResize((int) Math.round(heightView * value));
                return String.valueOf(width) + 'x' + height;
            })
            .toArray(String[]::new);

        this.pickerResolutions.setDisplayedValues(resolutions);
    }

    /**
     * Initializes the {@link #pickerThreads} field.
     *
     * @param pickerThreads The default value to put in the
     *                      {@link #pickerThreads} field.
     */
    private void initializePickerThreads(final int pickerThreads) {
        final int maxCores = UtilsContext.getNumOfCores(this);
        this.pickerThreads.setMinValue(1);
        this.pickerThreads.setMaxValue(maxCores);
        this.pickerThreads.setWrapSelectorWheel(true);
        this.pickerThreads.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerThreads.setValue(pickerThreads);
    }

    /**
     * Helper method that initializes the fields that are {@link View}s.
     */
    private void initializeViews() {
        this.drawView = findViewById(R.id.drawLayout);
        this.pickerScene = findViewById(R.id.pickerScene);
        this.pickerShader = findViewById(R.id.pickerShader);
        this.pickerSamplesPixel = findViewById(R.id.pickerSamplesPixel);
        this.pickerSamplesLight = findViewById(R.id.pickerSamplesLight);
        this.pickerAccelerator = findViewById(R.id.pickerAccelerator);
        this.pickerThreads = findViewById(R.id.pickerThreads);
        this.pickerResolutions = findViewById(R.id.pickerSize);
        this.checkBoxRasterize = findViewById(R.id.preview);
        validateViews();
    }

    /**
     * Helper method that validates the fields that are {@link View}s.
     */
    private void validateViews() {
        Preconditions.checkNotNull(this.pickerResolutions);
        Preconditions.checkNotNull(this.pickerThreads);
        Preconditions.checkNotNull(this.pickerAccelerator);
        Preconditions.checkNotNull(this.pickerSamplesLight);
        Preconditions.checkNotNull(this.pickerSamplesPixel);
        Preconditions.checkNotNull(this.pickerShader);
        Preconditions.checkNotNull(this.pickerScene);
        Preconditions.checkNotNull(this.drawView);
    }

}
