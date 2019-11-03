package puscas.mobilertapp;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * The main activity for Android User Interface.
 */
public final class MainActivity extends Activity {

    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.getName());

    static {
        try {
            System.loadLibrary("MobileRT");
            System.loadLibrary("Components");
            System.loadLibrary("AppInterface");
        } catch (final RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "WARNING: Could not load native library: " + ex.getMessage());
            System.exit(1);
        }
    }

    private DrawView drawView_ = null;
    private NumberPicker pickerScene_ = null;
    private NumberPicker pickerShader_ = null;
    private NumberPicker pickerThreads_ = null;
    private NumberPicker pickerAccelerator_ = null;
    private NumberPicker pickerSamplesPixel_ = null;
    private NumberPicker pickerSamplesLight_ = null;
    private NumberPicker pickerSizes_ = null;
    private CheckBox checkBoxRasterize_ = null;
    private String objFilePath_ = null;

    private native int resize(final int size);

    private static int getNumCoresOldPhones() {
        int res = 0;
        try {
            final File dir = new File("/sys/devices/system/cpu/");
            final File[] files = dir.listFiles(new MainActivity.CpuFilter());
            assert files != null;
            res = files.length;
        } catch (final RuntimeException ignored) {
            Log.e("getNumCoresOldPhones", "Can't get number of cores available!!!");
            System.exit(1);
        }
        return res;
    }

    private static int getNumberOfCores() {
        return (Build.VERSION.SDK_INT < 17) ? MainActivity.getNumCoresOldPhones() :
                Runtime.getRuntime().availableProcessors();
    }

    private void startStopRender(final String objFile) {
        final int scene = this.pickerScene_.getValue();
        final int shader = this.pickerShader_.getValue();
        final int threads = this.pickerThreads_.getValue();
        final int accelerator = this.pickerAccelerator_.getValue();
        final int samplesPixel = Integer.parseInt(this.pickerSamplesPixel_.getDisplayedValues()
                [this.pickerSamplesPixel_.getValue() - 1]);
        final int samplesLight = Integer.parseInt(this.pickerSamplesLight_.getDisplayedValues()
                [this.pickerSamplesLight_.getValue() - 1]);
        final String strResolution = this.pickerSizes_.getDisplayedValues()[this.pickerSizes_.getValue() - 1];
        final int width = Integer.parseInt(strResolution.substring(0, strResolution.indexOf('x')));
        final int height = Integer.parseInt(strResolution.substring(strResolution.indexOf('x') + 1));
        final String objText = objFile + ".obj";
        final String matText = objFile + ".mtl";
        final boolean rasterize = this.checkBoxRasterize_.isChecked();
        final int stage = this.drawView_.renderer_.viewText_.getState();

        switch (stage) {
            case 0:
            case 2:
            case 3://if ray tracer is idle
                this.drawView_.renderer_.viewText_.buttonRender_.setText(R.string.stop);
                this.drawView_.renderScene(scene, shader, threads, accelerator, samplesPixel, samplesLight, width, height,
                        objText, matText, rasterize);
                break;

            default://if ray tracer is busy
                this.drawView_.renderer_.viewText_.buttonRender_.setText(R.string.render);
                this.drawView_.stopDrawing();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        LOGGER.log(Level.INFO, "onRequestPermissionsResult");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.drawView_.onDestroy();

        LOGGER.log(Level.INFO, "onDestroy");
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            final Uri uri = data.getData();
            if (resultCode == Activity.RESULT_OK && uri != null) {
                final String sdCardDir = Environment.getExternalStorageDirectory() + "/";
                String filePath = uri.getEncodedPath();
                if (filePath != null) {
                    filePath = filePath.replace("%2F", "/");
                    filePath = filePath.replace("%3A", "/");
                    filePath = filePath.replace("/document/primary/", sdCardDir);
                    filePath = filePath.replace("/document", "/storage");

                    final int lastIndex = filePath.lastIndexOf('.');
                    this.objFilePath_ = filePath.substring(0, lastIndex);
                }
            }
        }
    }

    private void showFileChooser() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            final Intent intentChooseFile = Intent.createChooser(intent, "Select a File to Upload");
            this.startActivityForResult(intentChooseFile, 1);

            /*final String objFile = "conference/conference";
            final String objFile = "buddha/buddha";
            final String objFile = "powerplant/powerplant";
            final String objFile = "San_Miguel/san-miguel";
            String sdCardPath = Environment.getExternalStorageDirectory() + "/";
            sdCardPath = sdCardPath.replace("/storage/sdcard0", "/storage/extSdCard");
            sdCardPath = sdCardPath.replace("/emulated/0", "/1AE9-2819");
            final String filePath = sdCardPath + "WavefrontOBJs/" + objFile;
            startStopRender(filePath);*/
        } catch (final android.content.ActivityNotFoundException ex) {
            for (int i = 0; i < 6; ++i) {
                Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void CheckStoragePermission() {
        final int PERMISSION_STORAGE = 1;
        final int permissionCheckRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheckRead != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int scene = savedInstanceState.getInt("pickerScene");
        final int shader = savedInstanceState.getInt("pickerShader");
        final int threads = savedInstanceState.getInt("pickerThreads");
        final int accelerator = savedInstanceState.getInt("pickerAccelerator");
        final int samplesPixel = savedInstanceState.getInt("pickerSamplesPixel");
        final int samplesLight = savedInstanceState.getInt("pickerSamplesLight");
        final int sizes = savedInstanceState.getInt("pickerSizes");
        final boolean rasterize = savedInstanceState.getBoolean("checkBoxRasterize");
        this.pickerScene_.setValue(scene);
        this.pickerShader_.setValue(shader);
        this.pickerThreads_.setValue(threads);
        this.pickerAccelerator_.setValue(accelerator);
        this.pickerSamplesPixel_.setValue(samplesPixel);
        this.pickerSamplesLight_.setValue(samplesLight);
        this.pickerSizes_.setValue(sizes);
        this.checkBoxRasterize_.setChecked(rasterize);

        LOGGER.log(Level.INFO, "onRestoreInstanceState");
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        final int scene = this.pickerScene_.getValue();
        final int shader = this.pickerShader_.getValue();
        final int threads = this.pickerThreads_.getValue();
        final int accelerator = this.pickerAccelerator_.getValue();
        final int samplesPixel = this.pickerSamplesPixel_.getValue();
        final int samplesLight = this.pickerSamplesLight_.getValue();
        final int sizes = this.pickerSizes_.getValue();
        final boolean rasterize = this.checkBoxRasterize_.isChecked();
        outState.putInt("pickerScene", scene);
        outState.putInt("pickerShader", shader);
        outState.putInt("pickerThreads", threads);
        outState.putInt("pickerAccelerator", accelerator);
        outState.putInt("pickerSamplesPixel", samplesPixel);
        outState.putInt("pickerSamplesLight", samplesLight);
        outState.putInt("pickerSizes", sizes);
        outState.putBoolean("checkBoxRasterize", rasterize);
        this.drawView_.renderer_.finishRender();
        this.drawView_.renderer_.freeArrays();

        LOGGER.log(Level.INFO, "onSaveInstanceState");
    }

    private String readTextAsset(final String filename) {
        final AssetManager am = getAssets();
        String asset = null;
        try (final InputStream stream = am.open(filename)) {
            final int size = stream.available();
            final byte[] buffer = new byte[size];
            final int bytes = stream.read(buffer);
            if (bytes > 0) {
                asset = new String(buffer);
            }
        } catch (final OutOfMemoryError ex1) {
            Log.e("Assets", "Not enough memory for asset  " + filename);
            Log.e("Assets", Objects.requireNonNull(ex1.getMessage()));
            throw ex1;
        } catch (final IOException ex2) {
            Log.e("Assets", "Couldn't read asset " + filename);
            Log.e("Assets", Objects.requireNonNull(ex2.getMessage()));
            System.exit(1);
        }
        return asset;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (this.objFilePath_ != null) {
            this.startStopRender(this.objFilePath_);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.drawView_.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.drawView_.setPreserveEGLContextOnPause(true);
        this.drawView_.onPause();
        this.objFilePath_ = null;
    }

    private boolean checkGL20Support() {
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int defaultPickerScene = 0;
        int defaultPickerShader = 0;
        int defaultPickerThreads = 1;
        int defaultPickerAccelerator = 0;
        int defaultPickerSamplesPixel = 1;
        int defaultPickerSamplesLight = 1;
        int defaultPickerSizes = 4;
        boolean defaultCheckBoxRasterize = true;
        if (savedInstanceState != null) {
            defaultPickerScene = savedInstanceState.getInt("pickerScene");
            defaultPickerShader = savedInstanceState.getInt("pickerShader");
            defaultPickerThreads = savedInstanceState.getInt("pickerThreads");
            defaultPickerAccelerator = savedInstanceState.getInt("pickerAccelerator");
            defaultPickerSamplesPixel = savedInstanceState.getInt("pickerSamplesPixel");
            defaultPickerSamplesLight = savedInstanceState.getInt("pickerSamplesLight");
            defaultPickerSizes = savedInstanceState.getInt("pickerSizes");
            defaultCheckBoxRasterize = savedInstanceState.getBoolean("checkBoxRasterize");
        }

        try {
            this.setContentView(R.layout.activity_main);
        } catch (final RuntimeException ex) {
            Log.e("RuntimeException", Objects.requireNonNull(ex.getMessage()));
            System.exit(1);
        }

        final ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        final ConfigurationInfo info = am.getDeviceConfigurationInfo();
        final boolean supportES2 = (info.reqGlEsVersion >= 0x20000);
        if (supportES2 && this.checkGL20Support()) {
            this.drawView_ = this.findViewById(R.id.drawLayout);
            if (this.drawView_ == null) {
                Log.e("DrawView", "DrawView is NULL !!!");
                System.exit(1);
            }
            this.drawView_.setVisibility(View.INVISIBLE);
            this.drawView_.setEGLContextClientVersion(2);
            this.drawView_.setEGLConfigChooser(8, 8, 8, 8, 24, 0);

            this.drawView_.renderer_.bitmap_ = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            this.drawView_.renderer_.bitmap_.eraseColor(Color.DKGRAY);
            final String vertexShader = this.readTextAsset("Shaders/VertexShader.glsl");
            final String fragmentShader = this.readTextAsset("Shaders/FragmentShader.glsl");
            final String vertexShaderRaster = this.readTextAsset("Shaders/VertexShaderRaster.glsl");
            final String fragmentShaderRaster = this.readTextAsset("Shaders/FragmentShaderRaster.glsl");
            this.drawView_.renderer_.vertexShaderCode_ = vertexShader;
            this.drawView_.renderer_.fragmentShaderCode_ = fragmentShader;
            this.drawView_.renderer_.vertexShaderCodeRaster_ = vertexShaderRaster;
            this.drawView_.renderer_.fragmentShaderCodeRaster_ = fragmentShaderRaster;

            final TextView textView = this.findViewById(R.id.timeText);
            if (textView == null) {
                Log.e("ViewText", "ViewText is NULL !!!");
                System.exit(1);
            }
            final ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            this.drawView_.setViewAndActivityManager(textView, activityManager);

            this.drawView_.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            this.drawView_.setVisibility(View.VISIBLE);

            this.drawView_.renderer_.viewText_.buttonRender_ = this.findViewById(R.id.renderButton);
            if (this.drawView_.renderer_.viewText_.buttonRender_ == null) {
                Log.e("Button", "Button is NULL !!!");
                System.exit(1);
            }
            this.drawView_.renderer_.viewText_.buttonRender_.setOnLongClickListener((final View v) -> {
                this.recreate();
                return false;
            });

            this.drawView_.setPreserveEGLContextOnPause(true);
        } else {
            Log.e("OpenGLES 2", "Your device doesn't support ES 2. (" + info.reqGlEsVersion + ')');
            System.exit(1);
        }

        this.pickerScene_ = this.findViewById(R.id.pickerScene);
        if (this.pickerScene_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }

        this.pickerScene_.setMinValue(0);
        final String[] scenes = {"Cornell", "Spheres", "Cornell2", "Spheres2", "OBJ", "Test", "Wrong file"};
        this.pickerScene_.setMaxValue(scenes.length - 1);
        this.pickerScene_.setWrapSelectorWheel(true);
        this.pickerScene_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerScene_.setValue(defaultPickerScene);
        this.pickerScene_.setDisplayedValues(scenes);

        this.pickerShader_ = this.findViewById(R.id.pickerShader);
        if (this.pickerShader_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }

        this.pickerShader_.setMinValue(0);
        final String[] shaders = {"NoShadows", "Whitted", "PathTracer", "DepthMap", "Diffuse"};
        this.pickerShader_.setMaxValue(shaders.length - 1);
        this.pickerShader_.setWrapSelectorWheel(true);
        this.pickerShader_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerShader_.setValue(defaultPickerShader);
        this.pickerShader_.setDisplayedValues(shaders);

        final int maxSamplesPixel = 10;
        final String[] samplesPixel = new String[maxSamplesPixel];
        for (int i = 0; i < maxSamplesPixel; i++) {
            samplesPixel[i] = Integer.toString((i + 1) * (i + 1));
        }
        this.pickerSamplesPixel_ = this.findViewById(R.id.pickerSamplesPixel);
        if (this.pickerSamplesPixel_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        this.pickerSamplesPixel_.setMinValue(1);
        this.pickerSamplesPixel_.setMaxValue(maxSamplesPixel);
        this.pickerSamplesPixel_.setWrapSelectorWheel(true);
        this.pickerSamplesPixel_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerSamplesPixel_.setValue(defaultPickerSamplesPixel);
        this.pickerSamplesPixel_.setDisplayedValues(samplesPixel);

        final int maxSamplesLight = 100;
        final String[] samplesLight;
        try {
            samplesLight = new String[maxSamplesLight];
        } catch (final OutOfMemoryError ex) {
            ex.fillInStackTrace();
            Log.e("mobilertapp:", ex.getMessage());
            throw ex;
        }
        for (int i = 0; i < maxSamplesLight; i++) {
            samplesLight[i] = Integer.toString(i + 1);
        }
        this.pickerSamplesLight_ = this.findViewById(R.id.pickerSamplesLight);
        if (this.pickerSamplesLight_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        this.pickerSamplesLight_.setMinValue(1);
        this.pickerSamplesLight_.setMaxValue(maxSamplesLight);
        this.pickerSamplesLight_.setWrapSelectorWheel(true);
        this.pickerSamplesLight_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerSamplesLight_.setValue(defaultPickerSamplesLight);
        this.pickerSamplesLight_.setDisplayedValues(samplesLight);

        this.pickerAccelerator_ = this.findViewById(R.id.pickerAccelerator);
        if (this.pickerAccelerator_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        this.pickerAccelerator_.setMinValue(0);
        final String[] accelerators = {"Naive", "RegGrid", "BVH", "None"};
        this.pickerAccelerator_.setMaxValue(accelerators.length - 1);
        this.pickerAccelerator_.setWrapSelectorWheel(true);
        this.pickerAccelerator_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerAccelerator_.setValue(defaultPickerAccelerator);
        this.pickerAccelerator_.setDisplayedValues(accelerators);

        this.pickerThreads_ = this.findViewById(R.id.pickerThreads);
        if (this.pickerThreads_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        this.pickerThreads_.setMinValue(1);
        final int maxCores = MainActivity.getNumberOfCores();
        this.pickerThreads_.setMaxValue(maxCores);
        this.pickerThreads_.setWrapSelectorWheel(true);
        this.pickerThreads_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerThreads_.setValue(defaultPickerThreads);

        final int maxSizes = 9;
        final String[] sizes;
        try {
            sizes = new String[maxSizes];
        } catch (final OutOfMemoryError ex) {
            ex.fillInStackTrace();
            Log.e("mobilertapp:", ex.getMessage());
            throw ex;
        }
        sizes[0] = String.format(Locale.US, "%.2f", 0.05f) + 'x';
        for (int i = 2; i < maxSizes; i++) {
            final float value = (i + 1.0f) * 0.1f;
            sizes[i - 1] = String.format(Locale.US, "%.2f", value * value) + 'x';
        }
        sizes[maxSizes - 1] = String.format(Locale.US, "%.2f", 1.0f) + 'x';
        this.pickerSizes_ = this.findViewById(R.id.pickerSize);
        if (this.pickerSizes_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        this.pickerSizes_.setMinValue(1);
        this.pickerSizes_.setMaxValue(maxSizes);
        this.pickerSizes_.setWrapSelectorWheel(true);
        this.pickerSizes_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        this.pickerSizes_.setValue(defaultPickerSizes);

        this.checkBoxRasterize_ = this.findViewById(R.id.preview);
        this.checkBoxRasterize_.setChecked(defaultCheckBoxRasterize);

        final ViewTreeObserver vto = this.drawView_.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {
            final int width = this.drawView_.getWidth();
            final int height = this.drawView_.getHeight();

            float size = 0.05f;
            int currentWidth = this.resize(Math.round(width * size));
            int currentHeight = this.resize(Math.round(height * size));
            sizes[0] = Integer.toString(currentWidth)+ 'x' + currentHeight;

            for (int i = 2; i < maxSizes; i++) {
                size = (i + 1.0f) * 0.1f;
                currentWidth = this.resize(Math.round(width * size * size));
                currentHeight = this.resize(Math.round(height * size * size));
                sizes[i - 1] = "" + currentWidth + 'x' + currentHeight;
            }
            size = 1.0f;
            currentWidth = this.resize(Math.round(width * size * size));
            currentHeight = this.resize(Math.round(height * size * size));
            sizes[maxSizes - 1] = "" + currentWidth + 'x' + currentHeight;

            this.pickerSizes_.setDisplayedValues(sizes);
        });

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        }, 0);
    }

    public void startRender(@NonNull final View view) {
        this.objFilePath_ = "";
        final int scene = this.pickerScene_.getValue();
        final int isWorking = this.drawView_.renderer_.viewText_.getState();
        if (isWorking != 1) {
            if (scene == 4) {
                this.CheckStoragePermission();
                this.showFileChooser();
                return;
            } else if (scene == 5) {
                this.CheckStoragePermission();
                final String objFile = "conference/conference";
                //final String objFile = "teapot/teapot";
                //final String objFile = "buddha/buddha";
                //final String objFile = "powerplant/powerplant";
                //final String objFile = "San_Miguel/san-miguel";

                String sdCardPath = Environment.getExternalStorageDirectory() + "/";
                sdCardPath = sdCardPath.replace("/storage/sdcard0", "/storage/extSdCard");
                sdCardPath = sdCardPath.replace("/emulated/0", "/1D19-170B");
                this.objFilePath_ = sdCardPath + "WavefrontOBJs/" + objFile;
            }
        }

        this.startStopRender(this.objFilePath_);
    }

    private static final class CpuFilter implements FileFilter {
        CpuFilter() {
            super();
        }

        @Override
        public final boolean accept(final File pathname) {
            return Pattern.matches("cpu[0-9]+", pathname.getName());
        }
    }
}
