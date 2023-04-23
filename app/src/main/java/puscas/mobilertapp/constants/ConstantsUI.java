package puscas.mobilertapp.constants;

import android.os.Build;
import android.os.Bundle;

import java8.util.Optional;

/**
 * Utility class with constants for the User Interface (including
 * {@link android.widget.NumberPicker} used).
 */
public final class ConstantsUI {

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker}
     * for selecting the scene.
     */
    public static final String PICKER_SCENE = "pickerScene";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker}
     * for selecting the shader.
     */
    public static final String PICKER_SHADER = "pickerShader";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker}
     * for selecting the number of threads.
     */
    public static final String PICKER_THREADS = "pickerThreads";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker}
     * for selecting the accelerator.
     */
    public static final String PICKER_ACCELERATOR = "pickerAccelerator";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker}
     * for selecting the number of samples per pixel.
     */
    public static final String PICKER_SAMPLES_PIXEL = "pickerSamplesPixel";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker}
     * for selecting the number of samples per light.
     */
    public static final String PICKER_SAMPLES_LIGHT = "pickerSamplesLight";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker}
     * for selecting the resolution of the image.
     */
    public static final String PICKER_SIZE = "pickerSize";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.CheckBox}
     * to turn on/off the preview.
     */
    public static final String CHECK_BOX_RASTERIZE = "checkBoxRasterize";

    /**
     * The line separator in the current system.
     */
    public static final String LINE_SEPARATOR =
        Optional.ofNullable(System.getProperty("line.separator"))
            .orElse("\n");

    /**
     * The file separator in the current system.
     */
    public static final String FILE_SEPARATOR =
        Optional.ofNullable(System.getProperty("file.separator"))
            .orElse("/");

    /**
     * The path separator in the current system.
     */
    public static final String PATH_SEPARATOR =
        Optional.ofNullable(System.getProperty("path.separator"))
            .orElse(":");

    /**
     * The path to the shaders folder in the assets.
     */
    public static final String PATH_SHADERS = "Shaders";

    /**
     * The color for the UI's {@link android.widget.NumberPicker}.
     */
    static final String COLOR_NUMBER_PICKER = "#000000";

    /**
     * The text size for the UI's {@link android.widget.NumberPicker}.
     *
     * @implNote Needs to be above 12.5F in order to be compatible with Android API < 16.
     */
    static final float TEXT_SIZE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? 15.0F : 12.5F;

    /**
     * Private constructor to avoid creating instances.
     */
    private ConstantsUI() {
        throw new UnsupportedOperationException("Not implemented.");
    }

}
