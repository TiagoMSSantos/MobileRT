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
import java.util.regex.Pattern;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public final class MainActivity extends Activity {

    static {
        try {
            System.loadLibrary("MobileRT");
            System.loadLibrary("Components");
            System.loadLibrary("AppInterface");
        } catch (final Exception ex) {
            Log.e("LINK ERROR", "WARNING: Could not load native library: " + ex.getMessage());
            System.exit(1);
        }
    }

    private DrawView drawView_;
    private NumberPicker pickerScene_;
    private NumberPicker pickerShader_;
    private NumberPicker pickerThreads_;
    private NumberPicker pickerAccelerator_;
    private NumberPicker pickerSamplesPixel_;
    private NumberPicker pickerSamplesLight_;
    private NumberPicker pickerSizes_;
    private CheckBox checkBoxRasterize_;
    private String objFilePath_;

    private native int resize(final int size);

    private static int getNumCoresOldPhones() {
        int res = 0;
        try {
            final File dir = new File("/sys/devices/system/cpu/");
            final File[] files = dir.listFiles(new CpuFilter());
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
        final int scene = pickerScene_.getValue();
        final int shader = pickerShader_.getValue();
        final int threads = pickerThreads_.getValue();
        final int accelerator = pickerAccelerator_.getValue();
        final int samplesPixel = Integer.parseInt(pickerSamplesPixel_.getDisplayedValues()
                [pickerSamplesPixel_.getValue() - 1]);
        final int samplesLight = Integer.parseInt(pickerSamplesLight_.getDisplayedValues()
                [pickerSamplesLight_.getValue() - 1]);
        final String strResolution = pickerSizes_.getDisplayedValues()[pickerSizes_.getValue() - 1];
        final int width = Integer.parseInt(strResolution.substring(0, strResolution.indexOf('x')));
        final int height = Integer.parseInt(strResolution.substring(strResolution.indexOf('x') + 1));
        final String objText = objFile + ".obj";
        final String matText = objFile + ".mtl";
        final boolean rasterize = checkBoxRasterize_.isChecked();
        final int stage = drawView_.renderer_.viewText_.isWorking();

        switch (stage) {
            case 0:
            case 2:
            case 3://if ray tracer is idle
                drawView_.renderer_.viewText_.buttonRender_.setText(R.string.stop);
                drawView_.renderScene(scene, shader, threads, accelerator, samplesPixel, samplesLight, width, height,
                        objText, matText, rasterize);
                break;

            default://if ray tracer is busy
                drawView_.renderer_.viewText_.buttonRender_.setText(R.string.render);
                this.drawView_.stopDrawing();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onDestroy() {
        super.onDestroy();
        drawView_.onDestroy();
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
                    objFilePath_ = filePath.substring(0, lastIndex);
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
            startActivityForResult(intentChooseFile, 1);

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
            final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
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
        pickerScene_.setValue(scene);
        pickerShader_.setValue(shader);
        pickerThreads_.setValue(threads);
        pickerAccelerator_.setValue(accelerator);
        pickerSamplesPixel_.setValue(samplesPixel);
        pickerSamplesLight_.setValue(samplesLight);
        pickerSizes_.setValue(sizes);
        checkBoxRasterize_.setChecked(rasterize);
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        final int scene = pickerScene_.getValue();
        final int shader = pickerShader_.getValue();
        final int threads = pickerThreads_.getValue();
        final int accelerator = pickerAccelerator_.getValue();
        final int samplesPixel = pickerSamplesPixel_.getValue();
        final int samplesLight = pickerSamplesLight_.getValue();
        final int sizes = pickerSizes_.getValue();
        final boolean rasterize = checkBoxRasterize_.isChecked();
        savedInstanceState.putInt("pickerScene", scene);
        savedInstanceState.putInt("pickerShader", shader);
        savedInstanceState.putInt("pickerThreads", threads);
        savedInstanceState.putInt("pickerAccelerator", accelerator);
        savedInstanceState.putInt("pickerSamplesPixel", samplesPixel);
        savedInstanceState.putInt("pickerSamplesLight", samplesLight);
        savedInstanceState.putInt("pickerSizes", sizes);
        savedInstanceState.putBoolean("checkBoxRasterize", rasterize);
        drawView_.renderer_.finishRender();
        drawView_.renderer_.freeArrays();
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
        if (objFilePath_ != null) {
            startStopRender(objFilePath_);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawView_.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawView_.setPreserveEGLContextOnPause(true);
        drawView_.onPause();
        objFilePath_ = null;
    }

    private boolean checkGL20Support() {
        final EGL10 egl = (EGL10) EGLContext.getEGL();
        final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        final int[] version = new int[2];
        egl.eglInitialize(display, version);

        final int EGL_OPENGL_ES2_BIT = 4;
        final int[] configAttribs = {
                EGL10.EGL_RED_SIZE, 4,
                EGL10.EGL_GREEN_SIZE, 4,
                EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_NONE
        };

        final EGLConfig[] configs = new EGLConfig[10];
        final int[] num_config = new int[1];
        egl.eglChooseConfig(display, configAttribs, configs, 10, num_config);
        egl.eglTerminate(display);
        return num_config[0] > 0;
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
            setContentView(R.layout.activity_main);
        } catch (final RuntimeException ex) {
            Log.e("RuntimeException", Objects.requireNonNull(ex.getMessage()));
            System.exit(1);
        }

        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert am != null;
        final ConfigurationInfo info = am.getDeviceConfigurationInfo();
        final boolean supportES2 = (info.reqGlEsVersion >= 0x20000);
        if (supportES2 && checkGL20Support()) {
            drawView_ = findViewById(R.id.drawLayout);
            if (drawView_ == null) {
                Log.e("DrawView", "DrawView is NULL !!!");
                System.exit(1);
            }
            drawView_.setVisibility(View.INVISIBLE);
            drawView_.setEGLContextClientVersion(2);
            drawView_.setEGLConfigChooser(8, 8, 8, 8, 24, 0);

            drawView_.renderer_.bitmap_ = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            drawView_.renderer_.bitmap_.eraseColor(Color.DKGRAY);
            final String vertexShader = readTextAsset("Shaders/VertexShader.glsl");
            final String fragmentShader = readTextAsset("Shaders/FragmentShader.glsl");
            final String vertexShaderRaster = readTextAsset("Shaders/VertexShaderRaster.glsl");
            final String fragmentShaderRaster = readTextAsset("Shaders/FragmentShaderRaster.glsl");
            drawView_.renderer_.vertexShaderCode_ = vertexShader;
            drawView_.renderer_.fragmentShaderCode_ = fragmentShader;
            drawView_.renderer_.vertexShaderCodeRaster_ = vertexShaderRaster;
            drawView_.renderer_.fragmentShaderCodeRaster_ = fragmentShaderRaster;

            final TextView textView = findViewById(R.id.timeText);
            if (textView == null) {
                Log.e("ViewText", "ViewText is NULL !!!");
                System.exit(1);
            }
            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            drawView_.setViewAndActivityManager(textView, activityManager);

            drawView_.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            drawView_.setVisibility(View.VISIBLE);

            drawView_.renderer_.viewText_.buttonRender_ = findViewById(R.id.renderButton);
            if (drawView_.renderer_.viewText_.buttonRender_ == null) {
                Log.e("Button", "Button is NULL !!!");
                System.exit(1);
            }
            drawView_.renderer_.viewText_.buttonRender_.setOnLongClickListener((final View v) -> {
                this.recreate();
                return false;
            });

            drawView_.setPreserveEGLContextOnPause(true);
        } else {
            Log.e("OpenGLES 2", "Your device doesn't support ES 2. (" + info.reqGlEsVersion + ')');
            System.exit(1);
        }

        pickerScene_ = findViewById(R.id.pickerScene);
        if (pickerScene_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }

        pickerScene_.setMinValue(0);
        final String[] scenes = {"Cornell", "Spheres", "Cornell2", "Spheres2", "OBJ", "Test", "Wrong file"};
        pickerScene_.setMaxValue(scenes.length - 1);
        pickerScene_.setWrapSelectorWheel(true);
        pickerScene_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        pickerScene_.setValue(defaultPickerScene);
        pickerScene_.setDisplayedValues(scenes);

        pickerShader_ = findViewById(R.id.pickerShader);
        if (pickerShader_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }

        pickerShader_.setMinValue(0);
        final String[] shaders = {"NoShadows", "Whitted", "PathTracer", "DepthMap", "Diffuse"};
        pickerShader_.setMaxValue(shaders.length - 1);
        pickerShader_.setWrapSelectorWheel(true);
        pickerShader_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        pickerShader_.setValue(defaultPickerShader);
        pickerShader_.setDisplayedValues(shaders);

        final int maxSamplesPixel = 10;
        final String[] samplesPixel = new String[maxSamplesPixel];
        for (int i = 0; i < maxSamplesPixel; i++) {
            samplesPixel[i] = Integer.toString((i + 1) * (i + 1));
        }
        pickerSamplesPixel_ = findViewById(R.id.pickerSamplesPixel);
        if (pickerSamplesPixel_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        pickerSamplesPixel_.setMinValue(1);
        pickerSamplesPixel_.setMaxValue(maxSamplesPixel);
        pickerSamplesPixel_.setWrapSelectorWheel(true);
        pickerSamplesPixel_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        pickerSamplesPixel_.setValue(defaultPickerSamplesPixel);
        pickerSamplesPixel_.setDisplayedValues(samplesPixel);

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
        pickerSamplesLight_ = findViewById(R.id.pickerSamplesLight);
        if (pickerSamplesLight_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        pickerSamplesLight_.setMinValue(1);
        pickerSamplesLight_.setMaxValue(maxSamplesLight);
        pickerSamplesLight_.setWrapSelectorWheel(true);
        pickerSamplesLight_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        pickerSamplesLight_.setValue(defaultPickerSamplesLight);
        pickerSamplesLight_.setDisplayedValues(samplesLight);

        pickerAccelerator_ = findViewById(R.id.pickerAccelerator);
        if (pickerAccelerator_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        pickerAccelerator_.setMinValue(0);
        final String[] accelerators = {"Naive", "RegGrid", "BVH", "None"};
        pickerAccelerator_.setMaxValue(accelerators.length - 1);
        pickerAccelerator_.setWrapSelectorWheel(true);
        pickerAccelerator_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        pickerAccelerator_.setValue(defaultPickerAccelerator);
        pickerAccelerator_.setDisplayedValues(accelerators);

        pickerThreads_ = findViewById(R.id.pickerThreads);
        if (pickerThreads_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        pickerThreads_.setMinValue(1);
        final int maxCores = MainActivity.getNumberOfCores();
        pickerThreads_.setMaxValue(maxCores);
        pickerThreads_.setWrapSelectorWheel(true);
        pickerThreads_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        pickerThreads_.setValue(defaultPickerThreads);

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
        pickerSizes_ = findViewById(R.id.pickerSize);
        if (pickerSizes_ == null) {
            Log.e("NumberPicker", "NumberPicker is NULL !!!");
            System.exit(1);
        }
        pickerSizes_.setMinValue(1);
        pickerSizes_.setMaxValue(maxSizes);
        pickerSizes_.setWrapSelectorWheel(true);
        pickerSizes_.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        pickerSizes_.setValue(defaultPickerSizes);

        checkBoxRasterize_ = findViewById(R.id.preview);
        checkBoxRasterize_.setChecked(defaultCheckBoxRasterize);

        ViewTreeObserver vto = drawView_.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(() -> {
            final int width = drawView_.getWidth();
            final int height = drawView_.getHeight();

            float size = 0.05f;
            int currentWidth = resize(Math.round(width * size));
            int currentHeight = resize(Math.round(height * size));
            sizes[0] = "" + currentWidth + 'x' + currentHeight;
            for (int i = 2; i < maxSizes; i++) {
                size = (i + 1.0f) * 0.1f;
                currentWidth = resize(Math.round(width * size * size));
                currentHeight = resize(Math.round(height * size * size));
                sizes[i - 1] = "" + currentWidth + 'x' + currentHeight;
            }
            size = 1.0f;
            currentWidth = resize(Math.round(width * size * size));
            currentHeight = resize(Math.round(height * size * size));
            sizes[maxSizes - 1] = "" + currentWidth + 'x' + currentHeight;

            pickerSizes_.setDisplayedValues(sizes);
        });

        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        }, 0);
    }

    public void startRender(final View view) {
        objFilePath_ = "";
        final int scene = pickerScene_.getValue();
        final int isWorking = drawView_.renderer_.viewText_.isWorking();
        if (isWorking != 1) {
            if (scene == 4) {
                CheckStoragePermission();
                showFileChooser();
                return;
            } else if (scene == 5) {
                CheckStoragePermission();
                final String objFile = "conference/conference";
                //final String objFile = "buddha/buddha";
                //final String objFile = "powerplant/powerplant";
                //final String objFile = "San_Miguel/san-miguel";

                String sdCardPath = Environment.getExternalStorageDirectory() + "/";
                sdCardPath = sdCardPath.replace("/storage/sdcard0", "/storage/extSdCard");
                sdCardPath = sdCardPath.replace("/emulated/0", "/1D19-170B");
                objFilePath_ = sdCardPath + "WavefrontOBJs/" + objFile;
            }
        }

        startStopRender(objFilePath_);
    }

    private static final class CpuFilter implements FileFilter {
        CpuFilter() {
            super();
        }

        @Override
        public final boolean accept(final File file) {
            return Pattern.matches("cpu[0-9]+", file.getName());
        }
    }
}
