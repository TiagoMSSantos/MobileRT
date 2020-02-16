package puscas.mobilertapp;

import android.os.Bundle;

import java.util.logging.Logger;

/**
 * Utility class with constants for the User Interface (including {@link android.widget.NumberPicker} used).
 */
final class ConstantsUI {

    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(ConstantsUI.class.getName());

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker} for selecting the scene.
     */
    static final String PICKER_SCENE = "pickerScene";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker} for selecting the shader.
     */
    static final String PICKER_SHADER = "pickerShader";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker} for selecting the number of threads.
     */
    static final String PICKER_THREADS = "pickerThreads";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker} for selecting the accelerator.
     */
    static final String PICKER_ACCELERATOR = "pickerAccelerator";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker} for selecting the number of samples
     * per pixel.
     */
    static final String PICKER_SAMPLES_PIXEL = "pickerSamplesPixel";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker} for selecting the number of samples
     * per light.
     */
    static final String PICKER_SAMPLES_LIGHT = "pickerSamplesLight";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.NumberPicker} for selecting the resolution of the
     * image.
     */
    static final String PICKER_SIZES = "pickerSizes";

    /**
     * The key for the UI's {@link Bundle} of {@link android.widget.CheckBox} to turn on/off the preview.
     */
    static final String CHECK_BOX_RASTERIZE = "checkBoxRasterize";

    /**
     * The color for the UI's {@link android.widget.NumberPicker}.
     */
    static final String COLOR_NUMBER_PICKER = "#000000";

    /**
     * The text size for the UI's {@link android.widget.NumberPicker}.
     */
    static final float TEXT_SIZE = 15.0F;

    /**
     * A private constructor in order to prevent instantiating this helper class.
     */
    private ConstantsUI() {
        LOGGER.info("ConstantsUI");
    }
}
